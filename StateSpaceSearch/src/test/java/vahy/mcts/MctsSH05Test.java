package vahy.mcts;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.StateWrapper;
import vahy.api.policy.PolicyRecordBase;
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
import vahy.impl.predictor.DataTablePredictorWithLr;
import vahy.impl.search.AlphaZero.AlphaZeroDataMaker;
import vahy.impl.search.AlphaZero.AlphaZeroDataTablePredictor;
import vahy.impl.search.AlphaZero.AlphaZeroPolicyDefinitionSupplier;
import vahy.impl.search.MCTS.MCTSPolicyDefinitionSupplier;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

public class MctsSH05Test {


    @Test
    public void mctsTest() {

        var config = new SHConfigBuilder()
            .isModelKnown(true)
            .reward(100)
            .gameStringRepresentation(SHInstance.BENCHMARK_05)
            .maximalStepCountBound(500)
            .stepPenalty(1)
            .trapProbability(1.00)
            .buildConfig();


        var systemConfig = new SystemConfig(
            987568,
            false,
            Runtime.getRuntime().availableProcessors() - 1,
            false,
            10000,
            0,
            false,
            false,
            false,
            Path.of("TEST_PATH"),
            null);

        var algorithmConfig = new CommonAlgorithmConfig() {

            @Override
            public String toLog() {
                return "";
            }

            @Override
            public String toFile() {
                return "";
            }

            @Override
            public int getBatchEpisodeCount() {
                return 200;
            }

            @Override
            public int getStageCount() {
                return 100;
            }
        };

        double discountFactor = 1;

        var playerId = 1;
        var actionclass = SHAction.class;
        var cpuct = 1.0;
        var treeSearchCount = 1;

        var totalEntityCount = 2;

        var trainablePredictor = new DataTablePredictorWithLr(new double[totalEntityCount], 1.0);
        var episodeDataMaker = new VectorValueDataMaker<SHAction, SHState, PolicyRecordBase>(discountFactor, playerId);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetup = new PredictorTrainingSetup<>(
            playerId,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );

        var player = new MCTSPolicyDefinitionSupplier<SHAction, SHState>(actionclass, totalEntityCount)
            .getPolicyDefinition(playerId, 1, () -> 0.5, cpuct, treeSearchCount, predictorTrainingSetup);

        var roundBuilder = new RoundBuilder<SHConfig, SHAction, SHState, PolicyRecordBase, EpisodeStatisticsBase>()
            .setRoundName("SHIntegrationTest")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(config)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((SHConfig, splittableRandom) -> policyMode -> (new SHInstanceSupplier(config, splittableRandom)).createInitialState(policyMode))
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setStateStateWrapperInitializer(StateWrapper::new)
            .setPlayerPolicySupplierList(List.of(player));
        var result = roundBuilder.execute();

        Assert.assertTrue(result.getEvaluationStatistics().getTotalPayoffAverage().get(playerId) >= 288.0);
    }


    @Test
    public void alphaZeroTest() throws IOException, InterruptedException {

        var config = new SHConfigBuilder()
            .isModelKnown(true)
            .reward(100)
            .gameStringRepresentation(SHInstance.BENCHMARK_05)
            .maximalStepCountBound(500)
            .stepPenalty(1)
            .trapProbability(1.00)
            .buildConfig();


        var systemConfig = new SystemConfig(
            987568,
            false,
            Runtime.getRuntime().availableProcessors() - 1,
            false,
            10000,
            0,
            false,
            false,
            false,
            Path.of("TEST_PATH"),
            System.getProperty("user.home") + "/.local/virtualenvs/tensorflow_2_0/bin/python");

        var algorithmConfig = new CommonAlgorithmConfig() {

            @Override
            public String toLog() {
                return "";
            }

            @Override
            public String toFile() {
                return "";
            }

            @Override
            public int getBatchEpisodeCount() {
                return 200;
            }

            @Override
            public int getStageCount() {
                return 100;
            }
        };

        double discountFactor = 1;

        var playerId = 1;
        var actionClass = SHAction.class;
        var cpuct = 1.0;
        var treeSearchCount = 1;

        var totalEntityCount = 2;
        var totalActionCount = actionClass.getEnumConstants().length;
        var defaultPrediction = new double[totalEntityCount + totalActionCount];
        for (int i = totalEntityCount; i < defaultPrediction.length; i++) {
            defaultPrediction[i] = 1.0 / (totalActionCount);
        }

        var trainablePredictor = new AlphaZeroDataTablePredictor(defaultPrediction, 0.1, totalEntityCount);
        var episodeDataMaker = new AlphaZeroDataMaker<SHAction, SHState, PolicyRecordBase>(playerId, totalActionCount, discountFactor);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetup = new PredictorTrainingSetup<>(
            playerId,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );

        var player = new AlphaZeroPolicyDefinitionSupplier<SHAction, DoubleVector, SHState>(actionClass, totalEntityCount, config)
            .getPolicyDefinition(playerId, 1, cpuct, () -> 0.5, treeSearchCount, predictorTrainingSetup);

        var roundBuilder = new RoundBuilder<SHConfig, SHAction, SHState, PolicyRecordBase, EpisodeStatisticsBase>()
            .setRoundName("SHIntegrationTest")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(config)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((SHConfig, splittableRandom) -> policyMode -> (new SHInstanceSupplier(config, splittableRandom)).createInitialState(policyMode))
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setStateStateWrapperInitializer(StateWrapper::new)
            .setPlayerPolicySupplierList(List.of(player));
        var result = roundBuilder.execute();

        Assert.assertTrue(result.getEvaluationStatistics().getTotalPayoffAverage().get(playerId) >= 288.0);
    }


}
