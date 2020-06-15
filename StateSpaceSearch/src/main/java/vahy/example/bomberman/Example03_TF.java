package vahy.example.bomberman;

import vahy.api.experiment.ApproximatorConfig;
import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.learning.ApproximatorType;
import vahy.api.learning.dataAggregator.DataAggregationAlgorithm;
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
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.dataAggregator.ReplayBufferDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.ValueDataMaker;
import vahy.impl.learning.trainer.VectorValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.UniformRandomWalkPolicy;
import vahy.impl.policy.ValuePolicyDefinitionSupplier;
import vahy.impl.predictor.DataTablePredictor;
import vahy.impl.predictor.TrainableApproximator;
import vahy.impl.predictor.tf.TFHelper;
import vahy.impl.predictor.tf.TFModelImproved;
import vahy.impl.runner.PolicyDefinition;
import vahy.impl.search.AlphaGo.AlphaGoDataMaker;
import vahy.impl.search.AlphaGo.AlphaGoPolicyDefinitionSupplier;
import vahy.impl.search.AlphaGo.AlphaGoTablePredictor;
import vahy.impl.search.MCTS.MCTSPolicyDefinitionSupplier;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SplittableRandom;

public class Example03_TF {

    public static void main(String[] args) throws IOException, InvalidInstanceSetupException, InterruptedException {
        var config = new BomberManConfig(1000, true, 100, 1, 4, 3, 3, 1, 6, 0.1, BomberManInstance.BM_02);
        var systemConfig = new SystemConfig(987567, false, 7, true, 1000, 0, false, false, false, Path.of("TEST_PATH"),
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
                return 100;
            }
        };

        var environmentPolicyCount = config.getEnvironmentPolicyCount();

        var actionClass = BomberManAction.class;
        var discountFactor = 1.0;
        var rolloutCount = 1;
        var treeExpansionCount = 20;
        var cpuct = 1.0;

        var asdf = new BomberManInstanceInitializer(config, new SplittableRandom(0)).createInitialState(PolicyMode.TRAINING);
        int totalEntityCount = asdf.getTotalEntityCount();

        var mctsPolicySupplier = new MCTSPolicyDefinitionSupplier<BomberManAction, DoubleVector, BomberManState>(actionClass, totalEntityCount);
        var valuePolicySupplier = new ValuePolicyDefinitionSupplier<BomberManAction, BomberManState>();
        var alphaGoPolicySupplier = new AlphaGoPolicyDefinitionSupplier<BomberManAction, DoubleVector, BomberManState>(actionClass, totalEntityCount);



        var randomizedPlayer_0 = new PolicyDefinition<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase>(
            environmentPolicyCount + 0,
            1,
            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<BomberManAction, DoubleVector, BomberManState>(random, environmentPolicyCount + 0),
            new ArrayList<>());
//        var randomizedPlayer_1 = new PolicyDefinition<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase>(
//            environmentPolicyCount + 1,
//            1,
//            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<BomberManAction, DoubleVector, BomberManState>(random, environmentPolicyCount + 1),
//            new ArrayList<>());
//
//        var randomizedPlayer_2 = new PolicyDefinition<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase>(
//            environmentPolicyCount + 2,
//            1,
//            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<BomberManAction, DoubleVector, BomberManState>(random, environmentPolicyCount + 2),
//            new ArrayList<>());
//
//        var randomizedPlayer_3 = new PolicyDefinition<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase>(
//            environmentPolicyCount + 3,
//            1,
//            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<BomberManAction, DoubleVector, BomberManState>(random, environmentPolicyCount + 3),
//            new ArrayList<>());


        var trainablePredictor = new DataTablePredictor(new double[] {0.0});
        var episodeDataMaker = new ValueDataMaker<BomberManAction, BomberManState, PolicyRecordBase>(discountFactor, environmentPolicyCount + 1);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetup = new PredictorTrainingSetup<>(
            environmentPolicyCount + 1,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );
        var valuePolicyPlayer_1 = valuePolicySupplier.getPolicyDefinition(environmentPolicyCount + 1, 1, () -> 0.1, predictorTrainingSetup);
// ----------------------------------------------------------------------------------------


        var defaultPrediction_value = new double[] {0.0};
        var modelInputSize = asdf.getInGameEntityObservation(5).getObservedVector().length;

        var approximatorConfig = new ApproximatorConfig(
            "value/create_value_model.py",
            128,
            1,
            ApproximatorType.TF_NN,
            DataAggregationAlgorithm.REPLAY_BUFFER,
            10,
            0.01,
            0.8
        );

        var tfModelAsBytes = TFHelper.loadTensorFlowModel(approximatorConfig, systemConfig, modelInputSize, 0);
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


        var episodeDataMaker2 = new ValueDataMaker<BomberManAction, BomberManState, PolicyRecordBase>(discountFactor, environmentPolicyCount + 2);
//        var episodeDataMaker2 = new ValueMultiPolicyDataMaker<BomberManAction, BomberManState, PolicyRecordBase>(discountFactor, Set.of(
//            environmentPolicyCount + 2,
//            environmentPolicyCount + 2,
//            environmentPolicyCount + 3,
//            environmentPolicyCount + 4,
//            environmentPolicyCount + 5,
//            environmentPolicyCount + 6,
//            environmentPolicyCount + 7,
//            environmentPolicyCount + 8,
//            environmentPolicyCount + 9
//        ));

        var trainablePredictor2 = new TrainableApproximator(tfModel);
        var dataAggregator2 = new ReplayBufferDataAggregator(1000, new LinkedList<>());
        var trainablePredictor2_OLD = new DataTablePredictor(defaultPrediction_value);
        var dataAggregator2_OLD = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());


        var predictorTrainingSetup2 = new PredictorTrainingSetup<>(
            environmentPolicyCount + 2,
            trainablePredictor2,
            episodeDataMaker2,
            dataAggregator2
        );
        var valuePolicyPlayer_2 = valuePolicySupplier.getPolicyDefinition(environmentPolicyCount + 2, 1, () -> 0.1, predictorTrainingSetup2);
        var valuePolicyPlayer_3 = valuePolicySupplier.getPolicyDefinition(environmentPolicyCount + 3, 1, () -> 0.1, predictorTrainingSetup2);
        var valuePolicyPlayer_4 = valuePolicySupplier.getPolicyDefinition(environmentPolicyCount + 4, 1, () -> 0.1, predictorTrainingSetup2);
        var valuePolicyPlayer_5 = valuePolicySupplier.getPolicyDefinition(environmentPolicyCount + 5, 1, () -> 0.1, predictorTrainingSetup2);
//        var valuePolicyPlayer_6 = valuePolicySupplier.getPolicyDefinition(environmentPolicyCount + 5, 1, () -> 0.1, predictorTrainingSetup2);
//        var valuePolicyPlayer_7 = valuePolicySupplier.getPolicyDefinition(environmentPolicyCount + 6, 1, () -> 0.1, predictorTrainingSetup2);
//        var valuePolicyPlayer_8 = valuePolicySupplier.getPolicyDefinition(environmentPolicyCount + 7, 1, () -> 0.1, predictorTrainingSetup2);
//        var valuePolicyPlayer_9 = valuePolicySupplier.getPolicyDefinition(environmentPolicyCount + 8, 1, () -> 0.1, predictorTrainingSetup2);
//        var valuePolicyPlayer_10 = valuePolicySupplier.getPolicyDefinition(environmentPolicyCount + 9, 1, () -> 0.1, predictorTrainingSetup2);
// ----------------------------------------------------------------------------------------


//        var randomizedPlayer_1 = new PolicyDefinition<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase>(
//            environmentPolicyCount + 3,
//            1,
//            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<BomberManAction, DoubleVector, BomberManState>(random, environmentPolicyCount + 3),
//            new ArrayList<>());


        var trainablePredictorMCTSEval_1 = new DataTablePredictor(new double[totalEntityCount]);
        var episodeDataMakerMCTSEval_1 = new VectorValueDataMaker<BomberManAction, BomberManState, PolicyRecordBase>(discountFactor, environmentPolicyCount + 3);
        var dataAggregatorMCTSEval_1 = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetupMCTSEval_1 = new PredictorTrainingSetup<>(
            environmentPolicyCount + 3,
            trainablePredictorMCTSEval_1,
            episodeDataMakerMCTSEval_1,
            dataAggregatorMCTSEval_1
        );
// ----------------------------------------------------------------------------------------

        var trainablePredictorMCTSEval_2 = new DataTablePredictor(new double[totalEntityCount]);
        var episodeDataMakerMCTSEval_2 = new VectorValueDataMaker<BomberManAction, BomberManState, PolicyRecordBase>(discountFactor, environmentPolicyCount + 3);
        var dataAggregatorMCTSEval_2 = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetupMCTSEval_2 = new PredictorTrainingSetup<>(
            environmentPolicyCount + 3,
            trainablePredictorMCTSEval_2,
            episodeDataMakerMCTSEval_2,
            dataAggregatorMCTSEval_2
        );
// ----------------------------------------------------------------------------------------
        var mctsEvalPlayer_1 = mctsPolicySupplier.getPolicyDefinition(environmentPolicyCount + 3, 1, cpuct, treeExpansionCount, predictorTrainingSetupMCTSEval_1);
        var mctsEvalPlayer_2 = mctsPolicySupplier.getPolicyDefinition(environmentPolicyCount + 3, 1, cpuct, treeExpansionCount * 4, predictorTrainingSetupMCTSEval_2);

        var totalActionCount = actionClass.getEnumConstants().length;
        var defaultPrediction = new double[totalEntityCount + totalActionCount];
        for (int i = totalEntityCount; i < defaultPrediction.length; i++) {
            defaultPrediction[i] = 1.0 / (totalActionCount);
        }
        var trainablePredictorAlphaGoEval_1 = new AlphaGoTablePredictor(defaultPrediction, totalEntityCount, 0.1, totalActionCount);
        var episodeDataMakerAlphaGoEval_1 = new AlphaGoDataMaker<BomberManAction, BomberManState, PolicyRecordBase>(environmentPolicyCount + 4, discountFactor);
        var dataAggregatorAlphaGoEval_1 = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetupAlphaGoEval_2 = new PredictorTrainingSetup<>(
            environmentPolicyCount + 4,
            trainablePredictorAlphaGoEval_1,
            episodeDataMakerAlphaGoEval_1,
            dataAggregatorAlphaGoEval_1
        );

        var alphaGoPlayer_1 = alphaGoPolicySupplier.getPolicyDefinition(environmentPolicyCount + 4, 1, 1, treeExpansionCount, predictorTrainingSetupAlphaGoEval_2);
// ----------------------------------------------------------------------------------------
        var policyArgumentsList = List.of(
//            playerOneSupplier
//            mctsRolloutSupplier
            randomizedPlayer_0
//            ,randomizedPlayer_1
//            ,randomizedPlayer_2
//            ,randomizedPlayer_3
            ,valuePolicyPlayer_1
            ,valuePolicyPlayer_2
//            ,randomizedPlayer_1

            ,valuePolicyPlayer_3
            ,valuePolicyPlayer_4
            ,valuePolicyPlayer_5
//            ,valuePolicyPlayer_6
//            ,valuePolicyPlayer_7
//            ,valuePolicyPlayer_8
//            ,valuePolicyPlayer_9
//            ,valuePolicyPlayer_10

//            ,mctsEvalPlayer_1
//            ,mctsEvalPlayer_2
//            ,alphaGoPlayer_1
        );
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
