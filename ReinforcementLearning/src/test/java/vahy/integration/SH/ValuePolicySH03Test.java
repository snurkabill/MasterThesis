package vahy.integration.SH;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.SystemConfig;
import vahy.api.policy.PolicyMode;
import vahy.examples.simplifiedHallway.SHAction;
import vahy.examples.simplifiedHallway.SHConfigBuilder;
import vahy.examples.simplifiedHallway.SHInstance;
import vahy.examples.simplifiedHallway.SHInstanceSupplier;
import vahy.examples.simplifiedHallway.SHState;
import vahy.impl.RoundBuilder;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.ValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.ValuePolicy;
import vahy.impl.predictor.DataTablePredictorWithLr;
import vahy.impl.runner.PolicyDefinition;
import vahy.test.ConvergenceAssert;
import vahy.utils.JUnitParameterizedTestHelper;
import vahy.utils.StreamUtils;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

public class ValuePolicySH03Test {

    private PolicyDefinition<SHAction, DoubleVector, SHState> getPolicyDefinition() {
        var playerId = 1;
        double discountFactor = 1;

        var trainablePredictor = new DataTablePredictorWithLr(new double[]{0.0}, 0.2);
        var episodeDataMaker = new ValueDataMaker<SHAction, SHState>(discountFactor, playerId);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetup = new PredictorTrainingSetup<SHAction, DoubleVector, SHState>(
            playerId,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );

        return new PolicyDefinition<SHAction, DoubleVector, SHState>(
            playerId,
            1,
            (initialState, policyMode, policyId, random) -> {
                if (policyMode == PolicyMode.INFERENCE) {
                    return new ValuePolicy<>(random, policyId, trainablePredictor, 0.0);
                }
                return new ValuePolicy<>(random, policyId, trainablePredictor, 0.5);
            },
            List.of(predictorTrainingSetup)
        );
    }

    protected static Stream<Arguments> params() {
        return JUnitParameterizedTestHelper.cartesian(
            Stream.of(
                Arguments.of(0.0, 80.0, 80),
                Arguments.of(1.0, 60.0, 60),
                Arguments.of(0.05, 75.0, 76)
            ),
            StreamUtils.getSeedStream(5)
        );
    }

    @ParameterizedTest(name = "Trap probability {0} to reach [{1}, {2}] expectedPayoff with seed {3}")
    @MethodSource("params")
    public void convergence03Test(double trapProbability, double expectedMin, double expectedMax, long seed) {
        var config = new SHConfigBuilder()
            .isModelKnown(true)
            .reward(100)
            .gameStringRepresentation(SHInstance.BENCHMARK_03)
            .maximalStepCountBound(100)
            .stepPenalty(10)
            .trapProbability(trapProbability)
            .buildConfig();

        var systemConfig = new SystemConfig(
            seed,
            false,
            ConvergenceAssert.TEST_THREAD_COUNT,
            false,
            10000,
            0,
            false,
            false,
            false,
            Path.of("TEST_PATH"),
            null);

        var algorithmConfig = new CommonAlgorithmConfigBase(10, 50);

        var playerSupplier = getPolicyDefinition();
        var roundBuilder = RoundBuilder.getRoundBuilder("SHTest", config, systemConfig, algorithmConfig, List.of(playerSupplier), SHInstanceSupplier::new);
        var result = roundBuilder.execute();

        ConvergenceAssert.assertConvergenceResult(expectedMin, expectedMax, result.getEvaluationStatistics().getTotalPayoffAverage().get(playerSupplier.getPolicyId()), "Payoff");
    }
}
