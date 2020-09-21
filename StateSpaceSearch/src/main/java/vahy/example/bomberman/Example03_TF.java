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
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.dataAggregator.ReplayBufferDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.ValueDataMaker;
import vahy.impl.learning.trainer.VectorValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.UniformRandomWalkPolicy;
import vahy.impl.policy.ValuePolicyDefinitionSupplier;
import vahy.impl.policy.mcts.MCTSPolicyDefinitionSupplier;
import vahy.impl.predictor.DataTablePredictor;
import vahy.impl.predictor.DataTablePredictorWithLr;
import vahy.impl.predictor.TrainableApproximator;
import vahy.impl.predictor.tensorflow.TensorflowTrainablePredictor;
import vahy.impl.runner.PolicyDefinition;
import vahy.tensorflow.TFHelper;
import vahy.tensorflow.TFModelImproved;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SplittableRandom;

public class Example03_TF {

    private Example03_TF() {}

    public static void main(String[] args) throws IOException, InvalidInstanceSetupException, InterruptedException {
        var config = new BomberManConfig(1000, true, 100, 1, 4, 3, 3, 1, 5, 0.1, BomberManInstance.BM_01, PolicyShuffleStrategy.CATEGORY_SHUFFLE);
        var systemConfig = new SystemConfig(987567, false, 7, true, 1000, 0, false, false, false, Path.of("TEST_PATH"),
            System.getProperty("user.home") + "/.local/virtualenvs/tensorflow_2_0/bin/python");

        var algorithmConfig = new CommonAlgorithmConfigBase(100, 100);

        var environmentPolicyCount = config.getEnvironmentPolicyCount();

        var actionClass = BomberManAction.class;
        var discountFactor = 1.0;
        var treeExpansionCount = 100;
        var cpuct = 1.0;

        var asdf = new BomberManInstanceInitializer(config, new SplittableRandom(0)).createInitialState(PolicyMode.TRAINING);
        int totalEntityCount = asdf.getTotalEntityCount();

        var mctsPolicySupplier = new MCTSPolicyDefinitionSupplier<BomberManAction, BomberManState>(actionClass, totalEntityCount, config);
        var valuePolicySupplier = new ValuePolicyDefinitionSupplier<BomberManAction, BomberManState>();


        var randomizedPlayer_0 = new PolicyDefinition<BomberManAction, DoubleVector, BomberManState>(
            environmentPolicyCount + 0,
            1,
            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<BomberManAction, DoubleVector, BomberManState>(random, environmentPolicyCount + 0),
            new ArrayList<>());


        var trainablePredictor = new DataTablePredictor(new double[] {0.0});
        var episodeDataMaker = new ValueDataMaker<BomberManAction, BomberManState>(discountFactor, environmentPolicyCount + 1);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetup = new PredictorTrainingSetup<>(
            environmentPolicyCount + 1,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );
        var valuePolicyPlayer_1 = valuePolicySupplier.getPolicyDefinition(environmentPolicyCount + 1, 1, () -> 0.1, predictorTrainingSetup);
// ----------------------------------------------------------------------------------------
//        Paths.get("PythonScripts", "tensorflow_models", )

        var defaultPrediction_value = new double[] {0.0};
        var modelInputSize = asdf.getInGameEntityObservation(5).getObservedVector().length;

        var path = Paths.get("PythonScripts", "tensorflow_models", "value", "create_value_model.py");
        var tfModelAsBytes = TFHelper.loadTensorFlowModel(path, systemConfig.getPythonVirtualEnvPath(), systemConfig.getRandomSeed(), modelInputSize, 1, 0);
        var tfModel = new TFModelImproved(
            modelInputSize,
            defaultPrediction_value.length,
            8192,
            1,
            0.8,
            0.01,
            tfModelAsBytes,
            systemConfig.getParallelThreadsCount(),
            new SplittableRandom(systemConfig.getRandomSeed()));


        var episodeDataMaker2 = new ValueDataMaker<BomberManAction, BomberManState>(discountFactor, environmentPolicyCount + 2);

        var trainablePredictor2 = new TrainableApproximator(new TensorflowTrainablePredictor(tfModel));
        var dataAggregator2 = new ReplayBufferDataAggregator(1000);
//        var trainablePredictor2_OLD = new DataTablePredictor(defaultPrediction_value);
//        var dataAggregator2_OLD = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());


        var predictorTrainingSetup2 = new PredictorTrainingSetup<>(
            environmentPolicyCount + 2,
            trainablePredictor2,
            episodeDataMaker2,
            dataAggregator2
        );
        var valuePolicyPlayer_2 = valuePolicySupplier.getPolicyDefinition(environmentPolicyCount + 2, 1, () -> 0.1, predictorTrainingSetup2);
// ----------------------------------------------------------------------------------------


//        var path_ = Paths.get("PythonScripts", "tensorflow_models", "value", "create_value_model.py");
//        var tfModelAsBytes_ = TFHelper.loadTensorFlowModel(path_, systemConfig, modelInputSize, totalEntityCount, 0);
//        var tfModel_ = new TFModelImproved(
//            modelInputSize,
//            totalEntityCount,
//            1024,
//            10,
//            0.8,
//            0.1,
//            tfModelAsBytes_,
//            systemConfig.getParallelThreadsCount() * 2,
//            new SplittableRandom(systemConfig.getRandomSeed()));


//        var trainablePredictorMCTSEval_1 = new TrainableApproximator(tfModel_);
        var trainablePredictorMCTSEval_1 = new DataTablePredictorWithLr(new double[totalEntityCount], 0.1);
        var episodeDataMakerMCTSEval_1 = new VectorValueDataMaker<BomberManAction, BomberManState>(discountFactor, environmentPolicyCount + 3);
        var dataAggregatorMCTSEval_1 = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetupMCTSEval_1 = new PredictorTrainingSetup<>(
            environmentPolicyCount + 3,
            trainablePredictorMCTSEval_1,
            episodeDataMakerMCTSEval_1,
            dataAggregatorMCTSEval_1
        );
        var mctsEvalPlayer_1 = mctsPolicySupplier.getPolicyDefinition(environmentPolicyCount + 3, 1, () -> 0.1, cpuct, treeExpansionCount, predictorTrainingSetupMCTSEval_1, 2);
// ----------------------------------------------------------------------------------------

//        var trainablePredictorMCTSEval_2 = new TrainableApproximator(tfModel_);
        var trainablePredictorMCTSEval_2 = new DataTablePredictorWithLr(new double[totalEntityCount], 0.1);
        var episodeDataMakerMCTSEval_2 = new VectorValueDataMaker<BomberManAction, BomberManState>(discountFactor, environmentPolicyCount + 4);
        var dataAggregatorMCTSEval_2 = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetupMCTSEval_2 = new PredictorTrainingSetup<>(
            environmentPolicyCount + 4,
            trainablePredictorMCTSEval_2,
            episodeDataMakerMCTSEval_2,
            dataAggregatorMCTSEval_2
        );
        var mctsEvalPlayer_2 = mctsPolicySupplier.getPolicyDefinition(environmentPolicyCount + 4, 1, () -> 0.1, cpuct, treeExpansionCount, predictorTrainingSetupMCTSEval_2, 2);

// ----------------------------------------------------------------------------------------

//        var alphaGoPolicySupplier = new AlphaZeroPolicyDefinitionSupplier<BomberManAction, BomberManState>(actionClass, totalEntityCount, config);
//
//        var totalActionCount = actionClass.getEnumConstants().length;
//        var defaultPrediction = new double[totalEntityCount + totalActionCount];
//        for (int i = totalEntityCount; i < defaultPrediction.length; i++) {
//            defaultPrediction[i] = 1.0 / (totalActionCount);
//        }
//        var trainablePredictorAlphaGoEval_1 = new AlphaZeroDataTablePredictor(defaultPrediction, 0.1, totalEntityCount);
//        var episodeDataMakerAlphaGoEval_1 = new AlphaZeroDataMaker_V1<BomberManAction, BomberManState>(environmentPolicyCount + 4, totalActionCount, discountFactor);
//        var dataAggregatorAlphaGoEval_1 = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
//
//        var predictorTrainingSetupAlphaGoEval_2 = new PredictorTrainingSetup<>(
//            environmentPolicyCount + 4,
//            trainablePredictorAlphaGoEval_1,
//            episodeDataMakerAlphaGoEval_1,
//            dataAggregatorAlphaGoEval_1
//        );
//
//        var alphaGoPlayer_1 = alphaGoPolicySupplier.getPolicyDefinition(environmentPolicyCount + 4, 1, 1, () -> 0.1, treeExpansionCount, predictorTrainingSetupAlphaGoEval_2);
// ----------------------------------------------------------------------------------------
        var policyArgumentsList = List.of(
            randomizedPlayer_0
            ,valuePolicyPlayer_1
            ,valuePolicyPlayer_2
            ,mctsEvalPlayer_1
            ,mctsEvalPlayer_2
//            ,alphaGoPlayer_1
        );
        var roundBuilder = RoundBuilder.getRoundBuilder("BomberManExample01", config, systemConfig, algorithmConfig, policyArgumentsList, BomberManInstanceInitializer::new);

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
