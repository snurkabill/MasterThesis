package vahy.impl.learning.trainer.earlyStoppingStrategies;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.SystemConfig;
import vahy.api.policy.PolicyMode;
import vahy.examples.simplifiedHallway.*;
import vahy.impl.RoundBuilder;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.ValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.ValuePolicy;
import vahy.impl.predictor.DataTablePredictorWithLr;
import vahy.impl.runner.PolicyDefinition;
import vahy.test.ConvergenceAssert;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BoundedEarlyStoppingStrategyTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 10})
    public void stopSH3TrainingAfterUpperBoundIterations(int upperBound) {

        var config = new SHConfigBuilder()
                .isModelKnown(true)
                .reward(100)
                .gameStringRepresentation(SHInstance.BENCHMARK_03)
                .maximalStepCountBound(100)
                .stepPenalty(10)
                .trapProbability(0.5)
                .buildConfig();

        var systemConfig = new SystemConfig(
                46486,
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

        var algorithmConfig = new CommonAlgorithmConfigBase(20, 50);

        var playerSupplier = getPolicyDefinition();
        var roundBuilder = RoundBuilder.getRoundBuilder("SHTest", config, systemConfig, algorithmConfig, List.of(playerSupplier), SHInstanceSupplier::new);
        roundBuilder.setEarlyStoppingStrategy(new BoundedEarlyStoppingStrategy<>(upperBound));

        var result = roundBuilder.execute();

        assertEquals(upperBound, result.getTrainingStatisticsList().size());
    }

    private PolicyDefinition<SHAction, DoubleVector, SHState> getPolicyDefinition() {
        var playerId = 1;
        double discountFactor = 1;

        var trainablePredictor = new DataTablePredictorWithLr(new double[]{0.0}, 0.2);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
        var episodeDataMaker = new ValueDataMaker<SHAction, SHState>(discountFactor, playerId, dataAggregator);

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

}
