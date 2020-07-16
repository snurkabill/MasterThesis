package vahy.integration.mcts;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.Test;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.StateWrapper;
import vahy.examples.simplifiedHallway.SHAction;
import vahy.examples.simplifiedHallway.SHConfig;
import vahy.examples.simplifiedHallway.SHConfigBuilder;
import vahy.examples.simplifiedHallway.SHInstance;
import vahy.examples.simplifiedHallway.SHInstanceSupplier;
import vahy.examples.simplifiedHallway.SHState;
import vahy.impl.RoundBuilder;
import vahy.impl.benchmark.EpisodeStatisticsBase;
import vahy.impl.benchmark.EpisodeStatisticsCalculatorBase;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.VectorValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.mcts.MCTSPolicyDefinitionSupplier;
import vahy.impl.predictor.DataTablePredictorWithLr;
import vahy.impl.runner.PolicyDefinition;
import vahy.utils.StreamUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MCTSSingleVsBatchedEvaluatorTest {

    private PolicyDefinition<SHAction, DoubleVector, SHState> getPlayerSupplier(int batchSize) {

        var playerId = 1;
        double discountFactor = 1;

        var trainablePredictor = new DataTablePredictorWithLr(new double[]{0.0, 0.0}, 0.1);
        var episodeDataMaker = new VectorValueDataMaker<SHAction, SHState>(discountFactor, playerId);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetup = new PredictorTrainingSetup<SHAction, DoubleVector, SHState>(
            playerId,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );
        return new MCTSPolicyDefinitionSupplier<SHAction, SHState>(SHAction.class, 2, true).getPolicyDefinition(
            playerId,
            1,
            () -> 0.2,
            1,
            10,
            predictorTrainingSetup,
            batchSize
        );
    }

    private List<Double> runExperiment(PolicyDefinition<SHAction, DoubleVector, SHState> policy, long seed) {
        var config = new SHConfigBuilder()
            .isModelKnown(true)
            .reward(100)
            .gameStringRepresentation(SHInstance.BENCHMARK_12)
            .maximalStepCountBound(100)
            .stepPenalty(1)
            .trapProbability(0.1)
            .buildConfig();

        var algorithmConfig = new CommonAlgorithmConfigBase(50, 50);

        var systemConfig = new SystemConfig(
            seed,
            false,
            Runtime.getRuntime().availableProcessors() - 1,
            false,
            50,
            0,
            false,
            false,
            false,
            Path.of("TEST_PATH"),
            null);

        var policyArgumentsList = List.of(policy);

        var roundBuilder = new RoundBuilder<SHConfig, SHAction, SHState, EpisodeStatisticsBase>()
            .setRoundName("SH05Test")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(config)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((SHConfig, splittableRandom) -> policyMode -> (new SHInstanceSupplier(config, splittableRandom)).createInitialState(policyMode))
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setStateStateWrapperInitializer(StateWrapper::new)
            .setPlayerPolicySupplierList(policyArgumentsList);
        var result = roundBuilder.execute();
        return result.getTrainingStatisticsList().stream().map(x -> x.getTotalPayoffAverage().get(policy.getPolicyId())).collect(Collectors.toList());
//        return result.getEvaluationStatistics().getTotalPayoffAverage().get(policy.getPolicyId());
    }

    @Test
    public void singleVsBatchedEvaluatorTest() {
        var seedStream = StreamUtils.getSeedStream(10);
        var trialCount = 5;

        var list = new ArrayList<List<Double>>();
        for (Long seed : (Iterable<Long>)seedStream::iterator) {
            System.out.println("seed: " + seed);
            var start = System.currentTimeMillis();
            List<Double> result = runExperiment(getPlayerSupplier(0), seed);
            System.out.println("Result: " + result);
            System.out.println(System.currentTimeMillis() - start);
            for (int i = 1; i <= trialCount; i++) {
                System.out.println("trial: " + i);
                start = System.currentTimeMillis();
                List<Double> tmp = runExperiment(getPlayerSupplier(i), seed);
                System.out.println("Result tmp: " + tmp);
                System.out.println(System.currentTimeMillis() - start);
                assertIterableEquals(result, tmp);
            }
            list.add(result);
        }
        assertNotEquals(1, list.stream().map(List::hashCode).distinct().count());
    }

}
