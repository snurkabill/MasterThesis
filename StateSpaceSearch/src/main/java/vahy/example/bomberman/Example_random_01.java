package vahy.example.bomberman;

import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.StateWrapper;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecordBase;
import vahy.examples.bomberman.BomberManAction;
import vahy.examples.bomberman.BomberManConfig;
import vahy.examples.bomberman.BomberManInstance;
import vahy.examples.bomberman.BomberManInstanceInitializer;
import vahy.examples.bomberman.BomberManState;
import vahy.impl.RoundBuilder;
import vahy.impl.benchmark.EpisodeStatisticsBase;
import vahy.impl.benchmark.EpisodeStatisticsCalculatorBase;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.episode.InvalidInstanceSetupException;
import vahy.impl.learning.dataAggregator.ReplayBufferDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.VectorValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.UniformRandomWalkPolicy;
import vahy.impl.policy.mcts.MCTSPolicyDefinitionSupplier;
import vahy.impl.predictor.TrainableApproximator;
import vahy.impl.predictor.tf.TFHelper;
import vahy.impl.predictor.tf.TFModelImproved;
import vahy.impl.runner.PolicyDefinition;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Example_random_01 {

    public static void main(String[] args) throws IOException, InvalidInstanceSetupException, InterruptedException {

        var config = new BomberManConfig(
            500,
            true,
            100,
            1,
            2,
            3,
            3,
            1,
            5,
            0.1,
            BomberManInstance.BM_02);
        var systemConfig = new SystemConfig(
            987567,
            false,
//            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() - 1,
            true,
            50_000,
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
                return 100;
            }

            @Override
            public int getStageCount() {
                return 1000;
            }
        };

        var environmentPolicyCount = config.getEnvironmentPolicyCount();

        var actionClass = BomberManAction.class;
        var discountFactor = 1.0;
        var rolloutCount = 1;
        var treeExpansionCount = 50;
        var cpuct = 1.0;

        var asdf = new BomberManInstanceInitializer(config, new SplittableRandom(0)).createInitialState(PolicyMode.TRAINING);
        int totalEntityCount = asdf.getTotalEntityCount();

        //----------------------------------------------------------------------------------------------
        //----------------------------------------------------------------------------------------------
        //----------------------------------------------------------------------------------------------
        //----------------------------------------------------------------------------------------------
        // VALUE WITH HAHSMAP
//        var valuePolicySupplier = new ValuePolicyDefinitionSupplier<BomberManAction, BomberManState>();
//        var trainablePredictor = new DataTablePredictor(new double[] {0.0});
//        var episodeDataMaker = new ValueDataMaker<BomberManAction, BomberManState, PolicyRecordBase>(discountFactor, environmentPolicyCount + 0);
//        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

//        var predictorTrainingSetup = new PredictorTrainingSetup<>(
//            environmentPolicyCount + 0,
//            trainablePredictor,
//            episodeDataMaker,
//            dataAggregator
//        );
//        var valuePolicyPlayer_1 = valuePolicySupplier.getPolicyDefinition(environmentPolicyCount + 0, 1, () -> 0.01, predictorTrainingSetup);
        // ----------------------------------------------------------------------------------------------
        // ----------------------------------------------------------------------------------------------
        // ----------------------------------------------------------------------------------------------

        // VALUE WITH APPROXIMATOR

//        var defaultPrediction_value = new double[] {0.0};
//        var modelInputSize = asdf.getInGameEntityObservation(5).getObservedVector().length;
//        var path = Paths.get("PythonScripts", "tensorflow_models", "value", "create_value_model.py");
//        var tfModelAsBytes = TFHelper.loadTensorFlowModel(path, systemConfig, modelInputSize, 1, 0);
//        var tfModel = new TFModelImproved(
//            modelInputSize,
//            defaultPrediction_value.length,
//            8192,
//            1,
//            0.8,
//            0.01,
//            tfModelAsBytes,
//            systemConfig.getParallelThreadsCount(),
//            new SplittableRandom(systemConfig.getRandomSeed()));
//
//        var trainablePredictor2 = new TrainableApproximator(tfModel);
//        var episodeDataMaker2 = new ValueDataMaker<BomberManAction, BomberManState, PolicyRecordBase>(1.0, environmentPolicyCount + 0);
//        var dataAggregator2 = new ReplayBufferDataAggregator(1000, new LinkedList<>());
//
//        var predictorTrainingSetup2 = new PredictorTrainingSetup<>(
//            environmentPolicyCount + 0,
//            trainablePredictor2,
//            episodeDataMaker2,
//            dataAggregator2
//        );
//        var valuePolicyPlayer_1 = valuePolicySupplier.getPolicyDefinition(environmentPolicyCount + 0, 1, () -> 0.01, predictorTrainingSetup2);
//
//


        // ----------------------------------------------------------------------------------------------
        // ----------------------------------------------------------------------------------------------
        // ----------------------------------------------------------------------------------------------
        // ALPHAZERO WITH APPROXIMATOR

//        var modelInputSize = asdf.getInGameEntityObservation(5).getObservedVector().length;
//        var totalActionCount = actionClass.getEnumConstants().length;
//        var defaultPrediction_alpha = new double[totalEntityCount + totalActionCount];
//        for (int i = totalEntityCount; i < defaultPrediction_alpha.length; i++) {
//            defaultPrediction_alpha[i] = 1.0 / (totalActionCount);
//        }
//
//
//        var path = Paths.get("PythonScripts", "tensorflow_models", "alphazero", "create_alphazero_prototype.py");
//        var tfModelAsBytes = TFHelper.loadTensorFlowModel(path, systemConfig, modelInputSize, totalEntityCount, totalActionCount);
//        var tfModel = new TFModelImproved(
//            modelInputSize,
//            defaultPrediction_alpha.length,
//            8192,
//            1,
//            0.8,
//            0.01,
//            tfModelAsBytes,
//            systemConfig.getParallelThreadsCount(),
//            new SplittableRandom(systemConfig.getRandomSeed()));
//
//
//        var trainablePredictorAlphaGoEval_1 = new TrainableApproximator(tfModel);
//        var episodeDataMakerAlphaGoEval_1 = new AlphaZeroDataMaker<BomberManAction, BomberManState, PolicyRecordBase>(environmentPolicyCount + 0, totalActionCount, discountFactor);
//        var dataAggregatorAlphaGoEval_1 = new ReplayBufferDataAggregator(1000, new LinkedList<>());
//
//        var predictorTrainingSetupAlphaGoEval_2 = new PredictorTrainingSetup<>(
//            environmentPolicyCount + 0,
//            trainablePredictorAlphaGoEval_1,
//            episodeDataMakerAlphaGoEval_1,
//            dataAggregatorAlphaGoEval_1
//        );
//        var alphaGoPolicySupplier = new AlphaZeroPolicyDefinitionSupplier<BomberManAction, BomberManState>(actionClass, totalEntityCount, config);
//
//        var alphaGoPlayer_1 = alphaGoPolicySupplier.getPolicyDefinition(environmentPolicyCount + 0, 1, 1, () -> 0.1, treeExpansionCount, predictorTrainingSetupAlphaGoEval_2, 2);


        // ----------------------------------------------------------------------------------------------
        // ----------------------------------------------------------------------------------------------
        // ----------------------------------------------------------------------------------------------

        // MCTS WITH APPROXIMATOR

        var modelInputSize = asdf.getInGameEntityObservation(5).getObservedVector().length;
        var totalActionCount = actionClass.getEnumConstants().length;
        var path = Paths.get("PythonScripts", "tensorflow_models", "value", "create_value_vectorized_model.py");
        var tfModelAsBytes = TFHelper.loadTensorFlowModel(path, systemConfig, modelInputSize, totalEntityCount, totalActionCount);
        var tfModel = new TFModelImproved(
            modelInputSize,
            totalEntityCount,
            8192,
            1,
            0.8,
            0.01,
            tfModelAsBytes,
            systemConfig.getParallelThreadsCount(),
            new SplittableRandom(systemConfig.getRandomSeed()));


        var trainablePredictorMCTSEval_1 = new TrainableApproximator(tfModel);
        var defPred = new double[totalEntityCount];
        Arrays.fill(defPred, 10.0);
//        var trainablePredictorMCTSEval_1 = new DataTablePredictorWithLr(defPred, 0.1);
        var episodeDataMakerMCTSEval_1 = new VectorValueDataMaker<BomberManAction, BomberManState, PolicyRecordBase>(discountFactor, environmentPolicyCount + 0);
        var dataAggregatorMCTSEval_1 = new ReplayBufferDataAggregator(1000, new LinkedList<>());
//        var dataAggregatorMCTSEval_1 = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
        var predictorTrainingSetupMCTSEval_1 = new PredictorTrainingSetup<>(
            environmentPolicyCount + 0,
            trainablePredictorMCTSEval_1,
            episodeDataMakerMCTSEval_1,
            dataAggregatorMCTSEval_1
        );

        var batchEvalSize = 2;

        var mctsSupplier = new MCTSPolicyDefinitionSupplier<BomberManAction, BomberManState>(actionClass, totalEntityCount);
        var mctsPlayer_1 = mctsSupplier.getPolicyDefinition(environmentPolicyCount + 0, 1, () -> 0.1, cpuct, treeExpansionCount, predictorTrainingSetupMCTSEval_1, batchEvalSize);


        var policyArgumentsList = IntStream.of(1, 2, 3, 4).mapToObj(x -> new PolicyDefinition<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase>(
            environmentPolicyCount + x,
            1,
            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<BomberManAction, DoubleVector, BomberManState>(random, environmentPolicyCount + x),
            new ArrayList<>())).collect(Collectors.toList());

//        policyArgumentsList.add(0, valuePolicyPlayer_1);
//        policyArgumentsList.add(0, alphaGoPlayer_1);
        policyArgumentsList.add(0, mctsPlayer_1);



        var roundBuilder = new RoundBuilder<BomberManConfig, BomberManAction, BomberManState, PolicyRecordBase, EpisodeStatisticsBase>()
            .setRoundName("BomberManIntegrationTest")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(config)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((BomberManConfig, splittableRandom) -> policyMode -> (new BomberManInstanceInitializer(config, splittableRandom)).createInitialState(policyMode))
            .setStateStateWrapperInitializer(StateWrapper::new)
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setPlayerPolicySupplierList(policyArgumentsList);

        var start = System.currentTimeMillis();
        var result = roundBuilder.execute();
        var end = System.currentTimeMillis();
        System.out.println("Execution time: " + (end - start) + "[ms]");

        System.out.println(result.getEvaluationStatistics().getTotalPayoffAverage().get(1));


        List<Double> totalPayoffAverage = result.getEvaluationStatistics().getTotalPayoffAverage();

        for (int i = 0; i < totalPayoffAverage.size(); i++) {
            System.out.println("Policy" + i + " result: " + totalPayoffAverage.get(i));
        }

    }

}
