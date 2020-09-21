package vahy.example.bomberman;

import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.SystemConfig;
import vahy.api.policy.PolicyMode;
import vahy.examples.bomberman.BomberManAction;
import vahy.examples.bomberman.BomberManConfig;
import vahy.examples.bomberman.BomberManInstance;
import vahy.examples.bomberman.BomberManInstanceInitializer;
import vahy.examples.bomberman.BomberManState;
import vahy.impl.RoundBuilder;
import vahy.impl.episode.InvalidInstanceSetupException;
import vahy.impl.learning.dataAggregator.ReplayBufferDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.ValueDataMaker;
import vahy.impl.learning.trainer.VectorValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.ValuePolicyDefinitionSupplier;
import vahy.impl.policy.alphazero.AlphaZeroDataMaker_V1;
import vahy.impl.policy.alphazero.AlphaZeroPolicyDefinitionSupplier;
import vahy.impl.policy.mcts.MCTSPolicyDefinitionSupplier;
import vahy.impl.predictor.TrainableApproximator;
import vahy.impl.predictor.tensorflow.TensorflowTrainablePredictor;
import vahy.impl.runner.PolicyDefinition;
import vahy.tensorflow.TFHelper;
import vahy.tensorflow.TFModelImproved;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.SplittableRandom;

public class Example_random_01 {

    private Example_random_01() {}

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
            3,
            0.1,
            BomberManInstance.BM_02,
            PolicyShuffleStrategy.CATEGORY_SHUFFLE);
        var systemConfig = new SystemConfig(
            987567,
            false,
//            1,
            Runtime.getRuntime().availableProcessors(),
            true,
            50_000,
            0,
            false,
            false,
            false,
            Path.of("TEST_PATH"),
            System.getProperty("user.home") + "/.local/virtualenvs/tensorflow_2_0/bin/python");

        var algorithmConfig = new CommonAlgorithmConfigBase(1000, 100);

        var environmentPolicyCount = config.getEnvironmentPolicyCount();

        var actionClass = BomberManAction.class;
        var discountFactor = 1.0;
        var treeExpansionCount = 30;
        var cpuct = 1.0;

        var sampleState = new BomberManInstanceInitializer(config, new SplittableRandom(0)).createInitialState(PolicyMode.TRAINING);
        int totalEntityCount = sampleState.getTotalEntityCount();


//         ----------------------------------------------------------------------------------------------
//         ----------------------------------------------------------------------------------------------
//         ----------------------------------------------------------------------------------------------
//         VALUE WITH APPROXIMATOR

        var valuePolicy = getValuePolicy(systemConfig, environmentPolicyCount, sampleState);


        // ----------------------------------------------------------------------------------------------
        // ----------------------------------------------------------------------------------------------
        // ----------------------------------------------------------------------------------------------

        // MCTS WITH APPROXIMATOR

        var mctsPlayer_1 = getMCTSPolicy(config, systemConfig, environmentPolicyCount + 1, actionClass, discountFactor, treeExpansionCount, cpuct, sampleState, totalEntityCount, 2);


        // ----------------------------------------------------------------------------------------------
        // ----------------------------------------------------------------------------------------------
        // ----------------------------------------------------------------------------------------------
        // ALPHAZERO WITH APPROXIMATOR

        var alphaGoPolicy = getAlphaGoPolicy(config, systemConfig, environmentPolicyCount + 2, actionClass, discountFactor, cpuct, treeExpansionCount, sampleState, totalEntityCount, 2);



//        var randomPolicyList = IntStream.of(1, 2, 3, 4).mapToObj(x -> new PolicyDefinition<BomberManAction, DoubleVector, BomberManState>(
//            environmentPolicyCount + x,
//            1,
//            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<BomberManAction, DoubleVector, BomberManState>(random, environmentPolicyCount + x),
//            new ArrayList<>())).collect(Collectors.toList());


        var policyList = List.of(valuePolicy, mctsPlayer_1, alphaGoPolicy);

        var roundBuilder = RoundBuilder.getRoundBuilder("BomberManExample01", config, systemConfig, algorithmConfig, policyList, BomberManInstanceInitializer::new);

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

    private static PolicyDefinition<BomberManAction, DoubleVector, BomberManState> getMCTSPolicy(BomberManConfig bomBerManConfig,
                                                                                                                   SystemConfig systemConfig,
                                                                                                                   int policyId,
                                                                                                                   Class<BomberManAction> actionClass,
                                                                                                                   double discountFactor,
                                                                                                                   int treeExpansionCount,
                                                                                                                   double cpuct,
                                                                                                                   BomberManState sampleState,
                                                                                                                   int totalEntityCount,
                                                                                                                   int batchEvalSize) throws IOException, InterruptedException {
        var modelInputSize = sampleState.getInGameEntityObservation(5).getObservedVector().length;
        var totalActionCount = actionClass.getEnumConstants().length;
        var path = Paths.get("PythonScripts", "tensorflow_models", "value", "create_value_vectorized_model.py");
        var tfModelAsBytes = TFHelper.loadTensorFlowModel(path, systemConfig.getPythonVirtualEnvPath(), systemConfig.getRandomSeed(), modelInputSize, totalEntityCount, totalActionCount);
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


        var trainablePredictorMCTSEval_1 = new TrainableApproximator(new TensorflowTrainablePredictor(tfModel));
        var episodeDataMakerMCTSEval_1 = new VectorValueDataMaker<BomberManAction, BomberManState>(discountFactor, policyId);
        var dataAggregatorMCTSEval_1 = new ReplayBufferDataAggregator(1000);
        var predictorTrainingSetupMCTSEval_1 = new PredictorTrainingSetup<>(
            policyId,
            trainablePredictorMCTSEval_1,
            episodeDataMakerMCTSEval_1,
            dataAggregatorMCTSEval_1
        );


        var mctsSupplier = new MCTSPolicyDefinitionSupplier<BomberManAction, BomberManState>(actionClass, totalEntityCount, bomBerManConfig);
        return mctsSupplier.getPolicyDefinition(policyId, 1, () -> 0.1, cpuct, treeExpansionCount, predictorTrainingSetupMCTSEval_1, batchEvalSize);
    }

    private static PolicyDefinition<BomberManAction, DoubleVector, BomberManState> getAlphaGoPolicy(BomberManConfig config,
                                                                                                                      SystemConfig systemConfig,
                                                                                                                      int policyId,
                                                                                                                      Class<BomberManAction> actionClass,
                                                                                                                      double discountFactor,
                                                                                                                      double cpuct,
                                                                                                                      int treeExpansionCount,
                                                                                                                      BomberManState sampleState,
                                                                                                                      int totalEntityCount,
                                                                                                                      int evaluationDepth) throws IOException, InterruptedException {

        var modelInputSize = sampleState.getInGameEntityObservation(5).getObservedVector().length;
        var totalActionCount = actionClass.getEnumConstants().length;
        var defaultPrediction_alpha = new double[totalEntityCount + totalActionCount];
        for (int i = totalEntityCount; i < defaultPrediction_alpha.length; i++) {
            defaultPrediction_alpha[i] = 1.0 / totalActionCount;
        }

        var path = Paths.get("PythonScripts", "tensorflow_models", "alphazero", "create_alphazero_prototype.py");
        var tfModelAsBytes = TFHelper.loadTensorFlowModel(path, systemConfig.getPythonVirtualEnvPath(), systemConfig.getRandomSeed(), modelInputSize, totalEntityCount, totalActionCount);
        var tfModel = new TFModelImproved(
            modelInputSize,
            defaultPrediction_alpha.length,
            8192,
            1,
            0.8,
            0.01,
            tfModelAsBytes,
            systemConfig.getParallelThreadsCount(),
            new SplittableRandom(systemConfig.getRandomSeed()));


        var trainablePredictorAlphaGoEval_1 = new TrainableApproximator(new TensorflowTrainablePredictor(tfModel));
        var episodeDataMakerAlphaGoEval_1 = new AlphaZeroDataMaker_V1<BomberManAction, BomberManState>(policyId, totalActionCount, discountFactor);
        var dataAggregatorAlphaGoEval_1 = new ReplayBufferDataAggregator(1000);

        var predictorTrainingSetupAlphaGoEval_2 = new PredictorTrainingSetup<>(
            policyId,
            trainablePredictorAlphaGoEval_1,
            episodeDataMakerAlphaGoEval_1,
            dataAggregatorAlphaGoEval_1
        );
        var alphaGoPolicySupplier = new AlphaZeroPolicyDefinitionSupplier<BomberManAction, BomberManState>(actionClass, totalEntityCount, config);

        return alphaGoPolicySupplier.getPolicyDefinition(policyId, 1, cpuct, () -> 0.1, treeExpansionCount, predictorTrainingSetupAlphaGoEval_2, evaluationDepth);
    }


    private static PolicyDefinition<BomberManAction, DoubleVector, BomberManState> getValuePolicy(SystemConfig systemConfig, int policyId, BomberManState sampleState) throws IOException, InterruptedException {
        var modelInputSize = sampleState.getInGameEntityObservation(5).getObservedVector().length;
        var path = Paths.get("PythonScripts", "tensorflow_models", "value", "create_value_model.py");
        var tfModelAsBytes = TFHelper.loadTensorFlowModel(path, systemConfig.getPythonVirtualEnvPath(), systemConfig.getRandomSeed(), modelInputSize, 1, 0);
        var tfModel_value = new TFModelImproved(
            modelInputSize,
            1,
            8192,
            1,
            0.8,
            0.01,
            tfModelAsBytes,
            systemConfig.getParallelThreadsCount(),
            new SplittableRandom(systemConfig.getRandomSeed()));

        var trainablePredictor2 = new TrainableApproximator(new TensorflowTrainablePredictor(tfModel_value));
        var episodeDataMaker2 = new ValueDataMaker<BomberManAction, BomberManState>(1.0, policyId);
        var dataAggregator2 = new ReplayBufferDataAggregator(1000);

        var predictorTrainingSetup2 = new PredictorTrainingSetup<>(
            policyId,
            trainablePredictor2,
            episodeDataMaker2,
            dataAggregator2
        );
        var valuePolicySupplier = new ValuePolicyDefinitionSupplier<BomberManAction, BomberManState>();
        return valuePolicySupplier.getPolicyDefinition(policyId, 1, () -> 0.1, predictorTrainingSetup2);
    }

}
