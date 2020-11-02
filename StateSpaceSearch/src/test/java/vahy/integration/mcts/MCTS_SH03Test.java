package vahy.integration.mcts;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.examples.simplifiedHallway.SHAction;
import vahy.examples.simplifiedHallway.SHConfigBuilder;
import vahy.examples.simplifiedHallway.SHInstance;
import vahy.examples.simplifiedHallway.SHInstanceSupplier;
import vahy.examples.simplifiedHallway.SHState;
import vahy.impl.RoundBuilder;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.VectorValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.mcts.MCTSPolicyDefinitionSupplier;
import vahy.impl.predictor.DataTablePredictorWithLr;
import vahy.impl.runner.PolicyDefinition;
import vahy.test.ConvergenceAssert;
import vahy.utils.JUnitParameterizedTestHelper;
import vahy.utils.StreamUtils;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MCTS_SH03Test {

    private PolicyDefinition<SHAction, DoubleVector, SHState> getPolicyDefinition(ProblemConfig config, int treeExpansionCount) {
        var playerId = 1;

        var actionClass = SHAction.class;
        var totalEntityCount = 2;
        var totalActionCount = actionClass.getEnumConstants().length;
        var defaultPrediction = new double[totalEntityCount + totalActionCount];
        for (int i = totalEntityCount; i < defaultPrediction.length; i++) {
            defaultPrediction[i] = 1.0 / totalActionCount;
        }

        var trainablePredictor = new DataTablePredictorWithLr(new double[]{0.0, 0.0}, 0.25);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
        var episodeDataMaker = new VectorValueDataMaker<SHAction, SHState>(1, playerId, dataAggregator);

        var predictorTrainingSetup = new PredictorTrainingSetup<SHAction, DoubleVector, SHState>(
            playerId,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );
        Supplier<Double> explorationSupplier = () -> 0.5;
        return new MCTSPolicyDefinitionSupplier<SHAction, SHState>(SHAction.class, totalEntityCount, config).getPolicyDefinition(
            playerId,
            1,
            explorationSupplier,
            1,
            treeExpansionCount,
            predictorTrainingSetup
        );
    }

    public static Stream<Arguments> params() {
        return JUnitParameterizedTestHelper.cartesian(
            JUnitParameterizedTestHelper.cartesian(
                Stream.of(
                    Arguments.of(0.0, 80.0, 80),
                    Arguments.of(1.0, 60.0, 60),
                    Arguments.of(0.05, 74.0, 76)
                ),
                Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
            StreamUtils.getSeedStream(5)
        );
    }

    @ParameterizedTest(name = "Trap probability {0} to reach [{1}, {2}] expectedPayoff with tree update count {3} and seed {4}")
    @MethodSource("params")
    public void convergence03Test(double trapProbability, double expectedMin, double expectedMax, int treeUpdateCount, long seed) {
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

        var playerSupplier = getPolicyDefinition(config, treeUpdateCount);
        var roundBuilder = RoundBuilder.getRoundBuilder("MCTS_test_03", config, systemConfig, algorithmConfig, List.of(playerSupplier), SHInstanceSupplier::new);
        var result = roundBuilder.execute();

        ConvergenceAssert.assertConvergenceResult(expectedMin, expectedMax, result.getEvaluationStatistics().getTotalPayoffAverage().get(playerSupplier.getPolicyId()), "Payoff");
    }
}
