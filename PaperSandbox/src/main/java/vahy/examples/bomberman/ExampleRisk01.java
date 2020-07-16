package vahy.examples.bomberman;

import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.policy.PolicyMode;
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
import vahy.impl.policy.alphazero.AlphaZeroDataMaker_V1;
import vahy.impl.policy.alphazero.AlphaZeroDataTablePredictor;
import vahy.impl.policy.alphazero.AlphaZeroPolicyDefinitionSupplier;
import vahy.impl.policy.mcts.MCTSPolicyDefinitionSupplier;
import vahy.impl.predictor.DataTablePredictor;
import vahy.impl.predictor.DataTablePredictorWithLr;
import vahy.impl.predictor.TrainableApproximator;
import vahy.impl.predictor.tf.TFHelper;
import vahy.impl.predictor.tf.TFModelImproved;
import vahy.impl.runner.PolicyDefinition;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.paperGenerics.PaperStateWrapper;
import vahy.paperGenerics.PaperTreeUpdater;
import vahy.paperGenerics.evaluator.PaperNodeEvaluator;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.metadata.PaperMetadataFactory;
import vahy.paperGenerics.policy.PaperPolicyImpl;
import vahy.paperGenerics.policy.RiskAverseSearchTree;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.linearProgram.NoiseStrategy;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.paperGenerics.reinforcement.PaperDataTablePredictorWithLr;
import vahy.paperGenerics.reinforcement.learning.PaperEpisodeDataMaker_V2;
import vahy.paperGenerics.selector.PaperNodeSelector;
import vahy.utils.EnumUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class ExampleRisk01 {

    public static void main(String[] args) throws IOException, InvalidInstanceSetupException, InterruptedException {
        var config = new BomberManConfig(1000, true, 100, 1, 4, 3, 3, 1, 5, 0.1, BomberManInstance.BM_01, PolicyShuffleStrategy.NO_SHUFFLE);
        var systemConfig = new SystemConfig(987567, false, 1, true, 1000, 0, false, false, false, Path.of("TEST_PATH"),
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
                return 1;
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
        var treeExpansionCount = 100;
        var cpuct = 1.0;

        var asdf = new BomberManInstanceInitializer(config, new SplittableRandom(0)).createInitialState(PolicyMode.TRAINING);
        int totalEntityCount = asdf.getTotalEntityCount();

        var mctsPolicySupplier = new MCTSPolicyDefinitionSupplier<BomberManAction, BomberManRiskState>(actionClass, totalEntityCount, config);
        var valuePolicySupplier = new ValuePolicyDefinitionSupplier<BomberManAction, BomberManRiskState>();
        var alphaGoPolicySupplier = new AlphaZeroPolicyDefinitionSupplier<BomberManAction, BomberManRiskState>(actionClass, totalEntityCount, config);



        var randomizedPlayer_0 = new PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState>(
            environmentPolicyCount + 0,
            1,
            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<BomberManAction, DoubleVector, BomberManRiskState>(random, environmentPolicyCount + 0),
            new ArrayList<>());


        var trainablePredictor = new DataTablePredictor(new double[] {0.0});
        var episodeDataMaker = new ValueDataMaker<BomberManAction, BomberManRiskState>(discountFactor, environmentPolicyCount + 1);
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
        var tfModelAsBytes = TFHelper.loadTensorFlowModel(path, systemConfig, modelInputSize, 1, 0);
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


        var episodeDataMaker2 = new ValueDataMaker<BomberManAction, BomberManRiskState>(discountFactor, environmentPolicyCount + 2);

        var trainablePredictor2 = new TrainableApproximator(tfModel);
        var dataAggregator2 = new ReplayBufferDataAggregator(1000, new LinkedList<>());
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


        var path_ = Paths.get("PythonScripts", "tensorflow_models", "value", "create_value_model.py");
        var tfModelAsBytes_ = TFHelper.loadTensorFlowModel(path_, systemConfig, modelInputSize, totalEntityCount, 0);
        var tfModel_ = new TFModelImproved(
            modelInputSize,
            totalEntityCount,
            1024,
            10,
            0.8,
            0.1,
            tfModelAsBytes_,
            systemConfig.getParallelThreadsCount() * 2,
            new SplittableRandom(systemConfig.getRandomSeed()));


//        var trainablePredictorMCTSEval_1 = new TrainableApproximator(tfModel_);
        var trainablePredictorMCTSEval_1 = new DataTablePredictorWithLr(new double[totalEntityCount], 0.1);
        var episodeDataMakerMCTSEval_1 = new VectorValueDataMaker<BomberManAction, BomberManRiskState>(discountFactor, environmentPolicyCount + 3);
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
        var episodeDataMakerMCTSEval_2 = new VectorValueDataMaker<BomberManAction, BomberManRiskState>(discountFactor, environmentPolicyCount + 4);
        var dataAggregatorMCTSEval_2 = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetupMCTSEval_2 = new PredictorTrainingSetup<>(
            environmentPolicyCount + 4,
            trainablePredictorMCTSEval_2,
            episodeDataMakerMCTSEval_2,
            dataAggregatorMCTSEval_2
        );
        var mctsEvalPlayer_2 = mctsPolicySupplier.getPolicyDefinition(environmentPolicyCount + 4, 1, () -> 0.1, cpuct, treeExpansionCount, predictorTrainingSetupMCTSEval_2, 2);

// ----------------------------------------------------------------------------------------



        var totalActionCount = actionClass.getEnumConstants().length;
        var defaultPrediction = new double[totalEntityCount + totalActionCount];
        for (int i = totalEntityCount; i < defaultPrediction.length; i++) {
            defaultPrediction[i] = 1.0 / (totalActionCount);
        }
        var trainablePredictorAlphaGoEval_1 = new AlphaZeroDataTablePredictor(defaultPrediction, 0.1, totalEntityCount);
        var episodeDataMakerAlphaGoEval_1 = new AlphaZeroDataMaker_V1<BomberManAction, BomberManRiskState>(environmentPolicyCount + 4, totalActionCount, discountFactor);
        var dataAggregatorAlphaGoEval_1 = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetupAlphaGoEval_2 = new PredictorTrainingSetup<>(
            environmentPolicyCount + 4,
            trainablePredictorAlphaGoEval_1,
            episodeDataMakerAlphaGoEval_1,
            dataAggregatorAlphaGoEval_1
        );

        var alphaGoPlayer_1 = alphaGoPolicySupplier.getPolicyDefinition(environmentPolicyCount + 4, 1, 1, () -> 0.1, treeExpansionCount, predictorTrainingSetupAlphaGoEval_2);
// ----------------------------------------------------------------------------------------

        var riskAllowed = 1.0;
        var riskPolicyId = environmentPolicyCount + 5;
        var defaultPrediction_risk = new double[totalEntityCount * 2 + totalActionCount];
        for (int i = totalEntityCount * 2; i < defaultPrediction_risk.length; i++) {
            defaultPrediction_risk[i] = 1.0 / (totalActionCount);
        }

        var trainablePredictor_risk = new PaperDataTablePredictorWithLr(defaultPrediction_risk, 0.25, totalActionCount, totalEntityCount);
        var episodeDataMaker_risk = new PaperEpisodeDataMaker_V2<BomberManAction, BomberManRiskState>(discountFactor, totalActionCount, riskPolicyId);
        var dataAggregator_risk = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetup_risk = new PredictorTrainingSetup<BomberManAction, DoubleVector, BomberManRiskState>(
            riskPolicyId,
            trainablePredictor_risk,
            episodeDataMaker_risk,
            dataAggregator_risk
        );

        var metadataFactory = new PaperMetadataFactory<BomberManAction, DoubleVector, BomberManRiskState>(actionClass, totalEntityCount);
        var searchNodeFactory = new SearchNodeBaseFactoryImpl<BomberManAction, DoubleVector, PaperMetadata<BomberManAction>, BomberManRiskState>(actionClass, metadataFactory);

        var totalRiskAllowedInference = riskAllowed;
        Supplier<Double> explorationSupplier = () -> 1.0;
        Supplier<Double> temperatureSupplier = () -> 1.0;
        Supplier<Double> trainingRiskSupplier = () -> totalRiskAllowedInference;

        var treeUpdateConditionFactory = new FixedUpdateCountTreeConditionFactory(treeExpansionCount);

        var strategiesProvider = new StrategiesProvider<BomberManAction, DoubleVector, PaperMetadata<BomberManAction>, BomberManRiskState>(
            actionClass,
            InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW,
            InferenceNonExistingFlowStrategy.MAX_UCB_VALUE,
            ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE,
            ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VALUE_WITH_TEMPERATURE,
            FlowOptimizerType.HARD_HARD,
            SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY,
            NoiseStrategy.NOISY_05_06);

        var updater = new PaperTreeUpdater<BomberManAction, DoubleVector, BomberManRiskState>();
        var nodeEvaluator = new PaperNodeEvaluator<BomberManAction, PaperMetadata<BomberManAction>, BomberManRiskState>(searchNodeFactory, trainablePredictor_risk, config.isModelKnown());
        var cpuctParameter = 1.0;


        var riskPolicy =  new PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState>(
            riskPolicyId,
            1,
            (initialState_, policyMode_, policyId_, random_) -> {
                Supplier<PaperNodeSelector<BomberManAction, DoubleVector, BomberManRiskState>> nodeSelectorSupplier = () -> new PaperNodeSelector<>(random_, config.isModelKnown(), cpuctParameter, totalActionCount);
                var node = searchNodeFactory.createNode(initialState_, metadataFactory.createEmptyNodeMetadata(), new EnumMap<>(actionClass));
                switch(policyMode_) {
                    case INFERENCE:
                        return new PaperPolicyImpl<BomberManAction, DoubleVector, PaperMetadata<BomberManAction>, BomberManRiskState>(
                            policyId_,
                            random_,
                            treeUpdateConditionFactory.create(),
                            new RiskAverseSearchTree<BomberManAction, DoubleVector, PaperMetadata<BomberManAction>, BomberManRiskState>(
                                searchNodeFactory,
                                node,
                                nodeSelectorSupplier.get(),
                                updater,
                                nodeEvaluator,
                                random_,
                                totalRiskAllowedInference,
                                strategiesProvider));
                    case TRAINING:
                        return new PaperPolicyImpl<BomberManAction, DoubleVector, PaperMetadata<BomberManAction>, BomberManRiskState>(
                            policyId_,
                            random_,
                            treeUpdateConditionFactory.create(),
                            new RiskAverseSearchTree<BomberManAction, DoubleVector, PaperMetadata<BomberManAction>, BomberManRiskState>(
                                searchNodeFactory,
                                node,
                                nodeSelectorSupplier.get(),
                                updater,
                                nodeEvaluator,
                                random_,
                                trainingRiskSupplier.get(),
                                strategiesProvider),
                            explorationSupplier.get(),
                            temperatureSupplier.get());
                    default: throw EnumUtils.createExceptionForNotExpectedEnumValue(policyMode_);
                }
            },
            List.of(predictorTrainingSetup_risk)
        );

        // ----------------------------------------------------------------------------------------

        List<PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState>> policyArgumentsList = List.of(
            randomizedPlayer_0
            ,valuePolicyPlayer_1
            ,valuePolicyPlayer_2
            ,mctsEvalPlayer_1
            ,mctsEvalPlayer_2
//            ,alphaGoPlayer_1
//            ,riskPolicy
        );
        var roundBuilder = new RoundBuilder<BomberManConfig, BomberManAction, BomberManRiskState, EpisodeStatisticsBase>()
            .setRoundName("BomberManIntegrationTest")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(config)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((BomberManConfig, splittableRandom) -> policyMode -> (new BomberManRiskInstanceInitializer(config, splittableRandom)).createInitialState(policyMode))
            .setStateStateWrapperInitializer(PaperStateWrapper::new)
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
