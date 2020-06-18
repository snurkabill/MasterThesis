package vahy.mcts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.StateWrapper;
import vahy.api.policy.PolicyRecordBase;
import vahy.examples.tictactoe.TicTacToeAction;
import vahy.examples.tictactoe.TicTacToeConfig;
import vahy.examples.tictactoe.TicTacToeState;
import vahy.examples.tictactoe.TicTacToeStateInitializer;
import vahy.impl.RoundBuilder;
import vahy.impl.benchmark.EpisodeStatisticsBase;
import vahy.impl.benchmark.EpisodeStatisticsCalculatorBase;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.ValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.UniformRandomWalkPolicy;
import vahy.impl.policy.ValuePolicyDefinitionSupplier;
import vahy.impl.predictor.DataTablePredictor;
import vahy.impl.runner.PolicyDefinition;
import vahy.impl.search.AlphaZero.AlphaZeroDataMaker;
import vahy.impl.search.AlphaZero.AlphaZeroDataTablePredictor;
import vahy.impl.search.AlphaZero.AlphaZeroPolicyDefinitionSupplier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class BaseAlphaZeroTest {

    @Test
    public void baseAlphaZeroOnTicTacToeTest() {

        var ticTacConfig = new TicTacToeConfig();
        var systemConfig = new SystemConfig(
            987568,
            false,
            Runtime.getRuntime().availableProcessors() - 1,
            false,
            10000,
            100,
            true,
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
                return 100;
            }

            @Override
            public int getStageCount() {
                return 100;
            }
        };

        var actionClass = TicTacToeAction.class;
        var discountFactor = 1.0;
        var rolloutCount = 1;
        var treeExpansionCount = 10;
        var cpuct = 1.0;

        var alphaZeroPolicySupplier = new AlphaZeroPolicyDefinitionSupplier<TicTacToeAction, DoubleVector, TicTacToeState>(actionClass, 2, ticTacConfig);
        var valuePolicySupplier = new ValuePolicyDefinitionSupplier<TicTacToeAction, TicTacToeState>();

        var totalEntityCount = 2;
        var totalActionCount = 9;
        var defaultPrediction = new double[totalEntityCount + totalActionCount];
        for (int i = totalEntityCount; i < defaultPrediction.length; i++) {
            defaultPrediction[i] = 1.0 / (totalActionCount);
        }

        var predictorSetup = new PredictorTrainingSetup<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase>(
            0,
            new AlphaZeroDataTablePredictor(defaultPrediction, 0.1, totalEntityCount),
            new AlphaZeroDataMaker<>(0, totalActionCount, 1.0),
            new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>())
        );

//        var playerOneSupplier = alphaZeroPolicySupplier.getPolicyDefinition(0, 1, cpuct, treeExpansionCount, predictorSetup);

        var randomizedPlayer_0 = new PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase>(
            0,
            1,
            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<TicTacToeAction, DoubleVector, TicTacToeState>(random, 0),
            new ArrayList<>());

        var playerOneSupplier =  alphaZeroPolicySupplier.getPolicyDefinition(0, 1, cpuct, () -> 0.1, treeExpansionCount, predictorSetup);
//        var playerOneSupplier =  randomizedPlayer_0;

//        var trainablePredictor = new DataTablePredictorWithLr(new double[] {0.0}, 0.1);
        var trainablePredictor = new DataTablePredictor(new double[] {0.0});
        var episodeDataMaker = new ValueDataMaker<TicTacToeAction, TicTacToeState, PolicyRecordBase>(discountFactor, 1);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetup = new PredictorTrainingSetup<>(
            1,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );

        var playerTwoSupplier =  valuePolicySupplier.getPolicyDefinition(1, 1, () -> 0.1, predictorTrainingSetup);

        var policyArgumentsList = List.of(
            playerOneSupplier,
            playerTwoSupplier
        );


        var roundBuilder = new RoundBuilder<TicTacToeConfig, TicTacToeAction, TicTacToeState, PolicyRecordBase, EpisodeStatisticsBase>()
            .setRoundName("TicTacToeIntegrationTest")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(ticTacConfig)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((ticTacToeConfig, splittableRandom) -> policyMode -> (new TicTacToeStateInitializer(ticTacConfig, splittableRandom)).createInitialState(policyMode))
            .setStateStateWrapperInitializer(StateWrapper::new)
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setPlayerPolicySupplierList(policyArgumentsList);
        var result = roundBuilder.execute();

        System.out.println("AlphaZero policy result: " + result.getEvaluationStatistics().getTotalPayoffAverage().get(0));
        System.out.println("Value policy result: " + result.getEvaluationStatistics().getTotalPayoffAverage().get(1));

        assertTrue(result.getEvaluationStatistics().getTotalPayoffAverage().get(0) > result.getEvaluationStatistics().getTotalPayoffAverage().get(1));
        assertEquals(result.getEvaluationStatistics().getTotalPayoffAverage().get(0) + result.getEvaluationStatistics().getTotalPayoffAverage().get(1), 0.0, Math.pow(10, -10));
//        assertTrue(result.getEvaluationStatistics().getTotalPayoffAverage().get(1) > 0.3);
    }

//    @Test
//    public void functionApproximationAlphaZeroOnTicTacToeTest() throws IOException, InterruptedException {
//
//        var ticTacConfig = new TicTacToeConfig();
//        var systemConfig = new SystemConfig(
//            987568,
//            false,
//            Runtime.getRuntime().availableProcessors() - 1,
//            false,
//            10000,
//            100,
//            true,
//            false,
//            false,
//            Path.of("TEST_PATH"),
//            System.getProperty("user.home") + "/.local/virtualenvs/tensorflow_2_0/bin/python");
//
//        var algorithmConfig = new CommonAlgorithmConfig() {
//
//            @Override
//            public String toLog() {
//                return "";
//            }
//
//            @Override
//            public String toFile() {
//                return "";
//            }
//
//            @Override
//            public int getBatchEpisodeCount() {
//                return 200;
//            }
//
//            @Override
//            public int getStageCount() {
//                return 100;
//            }
//        };
//
//        var actionClass = TicTacToeAction.class;
//        var discountFactor = 1.0;
//        var rolloutCount = 1;
//        var treeExpansionCount = 20;
//        var cpuct = 1.0;
//
//        var alphaZeroPolicySupplier = new AlphaZeroPolicyDefinitionSupplier<TicTacToeAction, DoubleVector, TicTacToeState>(actionClass, 2, ticTacConfig);
//        var valuePolicySupplier = new ValuePolicyDefinitionSupplier<TicTacToeAction, TicTacToeState>();
//
//        var totalEntityCount = 2;
//        var totalActionCount = 9;
//        var defaultPrediction = new double[totalEntityCount + totalActionCount];
//        for (int i = totalEntityCount; i < defaultPrediction.length; i++) {
//            defaultPrediction[i] = 1.0 / (totalActionCount);
//        }
//
//        var asdf = new TicTacToeStateInitializer(ticTacConfig, new SplittableRandom(0)).createInitialState(PolicyMode.TRAINING);
//
//        var modelInputSize = asdf.getInGameEntityObservation(0).getObservedVector().length;
//
////        Path resourceDirectory = Paths.get("src","test","resources");
//        File resourcesDirectory = new File("src/test/resources");
//        var path = Paths.get(resourcesDirectory.getAbsolutePath(), "PythonScripts", "tensorflow_models", "alphazero", "create_alphazero_prototype.py");
////        var path = Paths.get("PythonScripts", "tensorflow_models", "alphazero", "create_alphazero_prototype.py");
//
//        var tfModelAsBytes = TFHelper.loadTensorFlowModel(path, systemConfig, modelInputSize, totalEntityCount, totalActionCount);
//        var tfModel = new TFModelImproved(
//            modelInputSize,
//            defaultPrediction.length,
//            16,
//            10,
//            0.9,
//            0.01,
//            tfModelAsBytes,
//            systemConfig.getParallelThreadsCount(),
//            new SplittableRandom(systemConfig.getRandomSeed()));
//
////        var trainablePredictor_alpha_zero = new AlphaZeroDataTablePredictor(defaultPrediction, 0.1, totalEntityCount),
//        var trainablePredictor_alpha_zero_2 = new TrainableApproximator(tfModel);
//        var dataAggregator_first_visit = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
////        var dataAggregator2 = new ReplayBufferDataAggregator(1000, new LinkedList<>());
//
//        var predictorSetup = new PredictorTrainingSetup<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase>(
//            0,
//            trainablePredictor_alpha_zero_2,
//            new AlphaZeroDataMaker<>(0, totalActionCount, 1.0),
//            dataAggregator_first_visit
//        );
//
////        var playerOneSupplier = alphaZeroPolicySupplier.getPolicyDefinition(0, 1, cpuct, treeExpansionCount, predictorSetup);
//
//        var randomizedPlayer_0 = new PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase>(
//            0,
//            1,
//            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<TicTacToeAction, DoubleVector, TicTacToeState>(random, 0),
//            new ArrayList<>());
//
//        var playerOneSupplier =  alphaZeroPolicySupplier.getPolicyDefinition(0, 1, cpuct, () -> 0.1, treeExpansionCount, predictorSetup);
////        var playerOneSupplier =  randomizedPlayer_0;
//
////        var trainablePredictor = new DataTablePredictorWithLr(new double[] {0.0}, 0.1);
//        var trainablePredictor = new DataTablePredictor(new double[] {0.0});
//        var episodeDataMaker = new ValueDataMaker<TicTacToeAction, TicTacToeState, PolicyRecordBase>(discountFactor, 1);
//        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
//
//        var predictorTrainingSetup = new PredictorTrainingSetup<>(
//            1,
//            trainablePredictor,
//            episodeDataMaker,
//            dataAggregator
//        );
//
//        var playerTwoSupplier =  valuePolicySupplier.getPolicyDefinition(1, 1, () -> 0.1, predictorTrainingSetup);
//
//        var policyArgumentsList = List.of(
//            playerOneSupplier,
//            playerTwoSupplier
//        );
//
//
//        var roundBuilder = new RoundBuilder<TicTacToeConfig, TicTacToeAction, TicTacToeState, PolicyRecordBase, EpisodeStatisticsBase>()
//            .setRoundName("TicTacToeIntegrationTest")
//            .setAdditionalDataPointGeneratorListSupplier(null)
//            .setCommonAlgorithmConfig(algorithmConfig)
//            .setProblemConfig(ticTacConfig)
//            .setSystemConfig(systemConfig)
//            .setProblemInstanceInitializerSupplier((ticTacToeConfig, splittableRandom) -> policyMode -> (new TicTacToeStateInitializer(ticTacConfig, splittableRandom)).createInitialState(policyMode))
//            .setStateStateWrapperInitializer(StateWrapper::new)
//            .setResultsFactory(new EpisodeResultsFactoryBase<>())
//            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
//            .setPlayerPolicySupplierList(policyArgumentsList);
//        var result = roundBuilder.execute();
//
//        System.out.println("AlphaZero policy result: " + result.getEvaluationStatistics().getTotalPayoffAverage().get(0));
//        System.out.println("Value policy result: " + result.getEvaluationStatistics().getTotalPayoffAverage().get(1));
//
//        assertTrue(result.getEvaluationStatistics().getTotalPayoffAverage().get(0) > result.getEvaluationStatistics().getTotalPayoffAverage().get(1));
//        assertEquals(result.getEvaluationStatistics().getTotalPayoffAverage().get(0) + result.getEvaluationStatistics().getTotalPayoffAverage().get(1), 0.0, Math.pow(10, -10));
////        assertTrue(result.getEvaluationStatistics().getTotalPayoffAverage().get(1) > 0.3);
//    }


//    @Test
//    @Ignore
//    public void functionApproximationAlphaZeroOnTicTacToe2Test() throws IOException, InterruptedException {
//
//        var ticTacConfig = new TicTacToeConfig();
//        var systemConfig = new SystemConfig(
//            987568,
//            false,
//            Runtime.getRuntime().availableProcessors() - 1,
//            true,
//            10000,
//            100,
//            true,
//            false,
//            false,
//            Path.of("TEST_PATH"),
//            System.getProperty("user.home") + "/.local/virtualenvs/tensorflow_2_0/bin/python");
//
//        var algorithmConfig = new CommonAlgorithmConfig() {
//
//            @Override
//            public String toLog() {
//                return "";
//            }
//
//            @Override
//            public String toFile() {
//                return "";
//            }
//
//            @Override
//            public int getBatchEpisodeCount() {
//                return 200;
//            }
//
//            @Override
//            public int getStageCount() {
//                return 100;
//            }
//        };
//
//        var actionClass = TicTacToeAction.class;
//        var discountFactor = 1.0;
//        var rolloutCount = 1;
//        var treeExpansionCount = 100;
//        var cpuct = 1.0;
//
//        var alphaZeroPolicySupplier = new AlphaZeroPolicyDefinitionSupplier<TicTacToeAction, DoubleVector, TicTacToeState>(actionClass, 2, ticTacConfig);
//        var valuePolicySupplier = new ValuePolicyDefinitionSupplier<TicTacToeAction, TicTacToeState>();
//
//        var totalEntityCount = 2;
//        var totalActionCount = 9;
//        var defaultPrediction = new double[totalEntityCount + totalActionCount];
//        for (int i = totalEntityCount; i < defaultPrediction.length; i++) {
//            defaultPrediction[i] = 1.0 / (totalActionCount);
//        }
//
//        var asdf = new TicTacToeStateInitializer(ticTacConfig, new SplittableRandom(0)).createInitialState(PolicyMode.TRAINING);
//
//        var defaultPrediction_value = new double[] {0.0};
//        var modelInputSize = asdf.getInGameEntityObservation(0).getObservedVector().length;
//
////        Path resourceDirectory = Paths.get("src","test","resources");
//        File resourcesDirectory = new File("src/test/resources");
//        var path = Paths.get(resourcesDirectory.getAbsolutePath(), "PythonScripts", "tensorflow_models", "alphazero", "create_alphazero_prototype.py");
////        var path = Paths.get("PythonScripts", "tensorflow_models", "alphazero", "create_alphazero_prototype.py");
//
//        var tfModelAsBytes = TFHelper.loadTensorFlowModel(path, systemConfig, modelInputSize, totalEntityCount, totalActionCount);
//        var tfModel = new TFModelImproved(
//            modelInputSize,
//            defaultPrediction.length,
//            16,
//            10,
//            0.9,
//            0.01,
//            tfModelAsBytes,
//            systemConfig.getParallelThreadsCount(),
//            new SplittableRandom(systemConfig.getRandomSeed()));
//
////        var trainablePredictor_alpha_zero = new AlphaZeroDataTablePredictor(defaultPrediction, 0.1, totalEntityCount),
//        var trainablePredictor_alpha_zero_2 = new TrainableApproximator(tfModel);
//        var dataAggregator_first_visit = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
////        var dataAggregator2 = new ReplayBufferDataAggregator(1000, new LinkedList<>());
//
//        var predictorSetup = new PredictorTrainingSetup<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase>(
//            0,
//            trainablePredictor_alpha_zero_2,
//            new AlphaZeroDataMaker<>(0, totalActionCount, 1.0),
//            dataAggregator_first_visit
//        );
//
////        var playerOneSupplier = alphaZeroPolicySupplier.getPolicyDefinition(0, 1, cpuct, treeExpansionCount, predictorSetup);
//
//        var randomizedPlayer_0 = new PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase>(
//            0,
//            1,
//            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<TicTacToeAction, DoubleVector, TicTacToeState>(random, 0),
//            new ArrayList<>());
//
//        var playerOneSupplier =  alphaZeroPolicySupplier.getPolicyDefinition(0, 1, cpuct, () -> 0.3, treeExpansionCount, predictorSetup);
////        var playerOneSupplier =  randomizedPlayer_0;
//
////        var trainablePredictor = new DataTablePredictorWithLr(new double[] {0.0}, 0.1);
//        var trainablePredictor = new DataTablePredictor(new double[] {0.0});
//        var episodeDataMaker = new ValueDataMaker<TicTacToeAction, TicTacToeState, PolicyRecordBase>(discountFactor, 1);
//        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
//
//        var predictorTrainingSetup = new PredictorTrainingSetup<>(
//            1,
//            trainablePredictor,
//            episodeDataMaker,
//            dataAggregator
//        );
//
//        var playerTwoSupplier =  alphaZeroPolicySupplier.getPolicyDefinition(1, 1, 1.0, () -> 0.3, treeExpansionCount, predictorSetup);
//
//        var policyArgumentsList = List.of(
//            playerOneSupplier,
//            playerTwoSupplier
//        );
//
//
//        var roundBuilder = new RoundBuilder<TicTacToeConfig, TicTacToeAction, TicTacToeState, PolicyRecordBase, EpisodeStatisticsBase>()
//            .setRoundName("TicTacToeIntegrationTest")
//            .setAdditionalDataPointGeneratorListSupplier(null)
//            .setCommonAlgorithmConfig(algorithmConfig)
//            .setProblemConfig(ticTacConfig)
//            .setSystemConfig(systemConfig)
//            .setProblemInstanceInitializerSupplier((ticTacToeConfig, splittableRandom) -> policyMode -> (new TicTacToeStateInitializer(ticTacConfig, splittableRandom)).createInitialState(policyMode))
//            .setStateStateWrapperInitializer(StateWrapper::new)
//            .setResultsFactory(new EpisodeResultsFactoryBase<>())
//            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
//            .setPlayerPolicySupplierList(policyArgumentsList);
//        var result = roundBuilder.execute();
//
//        System.out.println("AlphaZero policy result: " + result.getEvaluationStatistics().getTotalPayoffAverage().get(0));
//        System.out.println("Value policy result: " + result.getEvaluationStatistics().getTotalPayoffAverage().get(1));
//
//        assertTrue(result.getEvaluationStatistics().getTotalPayoffAverage().get(0) >= result.getEvaluationStatistics().getTotalPayoffAverage().get(1));
//        assertEquals(result.getEvaluationStatistics().getTotalPayoffAverage().get(0) + result.getEvaluationStatistics().getTotalPayoffAverage().get(1), 0.0, Math.pow(10, -10));
////        assertTrue(result.getEvaluationStatistics().getTotalPayoffAverage().get(1) > 0.3);
//    }



}
