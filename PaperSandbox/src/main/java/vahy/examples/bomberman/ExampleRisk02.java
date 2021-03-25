package vahy.examples.bomberman;

import vahy.RiskStateWrapper;
import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.policy.PolicyMode;
import vahy.benchmark.RiskEpisodeStatistics;
import vahy.benchmark.RiskEpisodeStatisticsCalculator;
import vahy.impl.RoundBuilder;
import vahy.impl.benchmark.PolicyResults;
import vahy.impl.episode.DataPointGeneratorGeneric;
import vahy.impl.episode.EpisodeResultsFactoryBase;
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
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.ralph.RalphTreeUpdater;
import vahy.ralph.evaluator.RalphBatchNodeEvaluator;
import vahy.ralph.evaluator.RalphNodeEvaluator;
import vahy.ralph.metadata.RalphMetadata;
import vahy.ralph.metadata.RiskSearchMetadataFactory;
import vahy.ralph.policy.RalphPolicy;
import vahy.ralph.policy.RiskAverseSearchTree;
import vahy.ralph.policy.flowOptimizer.FlowOptimizerType;
import vahy.ralph.policy.linearProgram.NoiseStrategy;
import vahy.ralph.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.ralph.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.ralph.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.ralph.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.ralph.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.ralph.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.ralph.reinforcement.learning.RalphEpisodeDataMaker_V2;
import vahy.ralph.selector.RalphNodeSelector;
import vahy.tensorflow.TFHelper;
import vahy.tensorflow.TFModelImproved;
import vahy.utils.EnumUtils;
import vahy.utils.StreamUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExampleRisk02 {

    private ExampleRisk02() {}

    public static void main(String[] args) throws IOException, InvalidInstanceSetupException, InterruptedException {
        var config = new BomberManConfig(1000, true, 100, 1, 4, 3, 3, 1, 4, 0.1, BomberManInstance.BM_02, PolicyShuffleStrategy.NO_SHUFFLE);
        var systemConfig = new SystemConfig(987567, false, 7, true, 100, 100, true, false, false, Path.of("TEST_PATH"));

        var algorithmConfig = new CommonAlgorithmConfigBase(1000, 100);

        var environmentPolicyCount = config.getEnvironmentPolicyCount();

        var actionClass = BomberManAction.class;
        var totalActionCount = actionClass.getEnumConstants().length;
        var discountFactor = 1.0;
        var treeExpansionCount = 100;
        var cpuct = 1.0;

        var instance = new BomberManInstanceInitializer(config, new SplittableRandom(0)).createInitialState(PolicyMode.TRAINING);
        int totalEntityCount = instance.getTotalEntityCount();
        var modelInputSize = instance.getInGameEntityObservation(5).getObservedVector().length;

        var evaluator_batch_size = 2;

        var valuePolicy = getValuePolicy(systemConfig, environmentPolicyCount + 0, discountFactor, modelInputSize);
// ----------------------------------------------------------------------------------------

        var mctsEvalPlayer_1 = getMctsPolicy(totalEntityCount, modelInputSize, config, systemConfig, environmentPolicyCount + 1, discountFactor, treeExpansionCount, cpuct, totalEntityCount, evaluator_batch_size);
// ----------------------------------------------------------------------------------------

        var alphaGoPlayer_1 = getAlphaZeroPlayer(modelInputSize, totalActionCount, config, systemConfig, environmentPolicyCount + 2, totalActionCount, discountFactor, treeExpansionCount, totalEntityCount, evaluator_batch_size);
// ----------------------------------------------------------------------------------------

        var riskPolicy = getRiskPolicy(config, systemConfig, environmentPolicyCount + 3, actionClass, totalActionCount, discountFactor, treeExpansionCount, cpuct, totalEntityCount, modelInputSize, evaluator_batch_size, 0.0, 0.9);

        // ----------------------------------------------------------------------------------------

        List<PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState>> policyArgumentsList = Stream.of(
            valuePolicy
            ,mctsEvalPlayer_1
            ,alphaGoPlayer_1
            ,riskPolicy
        ).sorted(Comparator.comparing(PolicyDefinition::getPolicyId)).collect(Collectors.toList());


        var additionalStatistics = new DataPointGeneratorGeneric<RiskEpisodeStatistics>("Risk Hit Ratio", x -> StreamUtils.labelWrapperFunction(x.getRiskHitRatio()));

        var roundBuilder = RoundBuilder.getRoundBuilder(
            "BomberManRisk01",
            config,
            systemConfig,
            algorithmConfig,
            policyArgumentsList,
            List.of(additionalStatistics),
            BomberManRiskInstanceInitializer::new,
            RiskStateWrapper::new,
            new RiskEpisodeStatisticsCalculator<>(),
            new EpisodeResultsFactoryBase<>()
        );

        var start = System.currentTimeMillis();
        var result = roundBuilder.execute();
        var end = System.currentTimeMillis();
        System.out.println("Execution time: " + (end - start) + "[ms]");

        printProperty(result, RiskEpisodeStatistics::getTotalPayoffAverage, "TotalPayoffAverage");
        printProperty(result, RiskEpisodeStatistics::getRiskHitRatio, "RiskHitRatio");
        printProperty(result, RiskEpisodeStatistics::getRiskExhaustedIndexAverage, "ExhaustedRiskIndexAverage");
        printProperty(result, RiskEpisodeStatistics::getRiskThresholdAtEndAverage, "RiskThresholdAtEndAverage");
    }

    private static void printProperty(PolicyResults<BomberManAction, DoubleVector, BomberManRiskState, RiskEpisodeStatistics> results, Function<RiskEpisodeStatistics, List<Double>> getter, String propertyName) {
        List<Double> values = getter.apply(results.getEvaluationStatistics());
        System.out.println("Property: [" + propertyName + "]");
        for (int i = 0; i < values.size(); i++) {
            System.out.println("Policy" + i + ": " + values.get(i));
        }
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------");
    }

    private static PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState> getRiskPolicy(BomberManConfig config,
                                                                                                     SystemConfig systemConfig,
                                                                                                     int policyId,
                                                                                                     Class<BomberManAction> actionClass,
                                                                                                     int totalActionCount,
                                                                                                     double discountFactor,
                                                                                                     int treeExpansionCount,
                                                                                                     double cpuct,
                                                                                                     int totalEntityCount,
                                                                                                     int modelInputSize,
                                                                                                     int maxBatchedDepth,
                                                                                                     double risk,
                                                                                                     double riskDecay) throws IOException, InterruptedException {
        var riskAllowed = risk;

        var path_ = Paths.get("PythonScripts", "tensorflow_models", "riskBomberManExample02", "create_risk_model.py");

        var tfModelAsBytes_ = TFHelper.loadTensorFlowModel(path_, systemConfig.getRandomSeed(),  modelInputSize, totalEntityCount, totalActionCount);
        var tfModel_ = new TFModelImproved(
            modelInputSize,
            totalEntityCount * 2 + totalActionCount,
            8192,
            1,
            0.8,
            0.01,
            tfModelAsBytes_,
            systemConfig.getParallelThreadsCount(),
            new SplittableRandom(systemConfig.getRandomSeed()));

        var trainablePredictor_risk = new TrainableApproximator(new TensorflowTrainablePredictor(tfModel_));
        var dataAggregator_risk = new ReplayBufferDataAggregator(1000);
        var episodeDataMaker_risk = new RalphEpisodeDataMaker_V2<BomberManAction, BomberManRiskState>(policyId, totalActionCount, discountFactor, dataAggregator_risk);
//        var dataAggregator_risk = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetup_risk = new PredictorTrainingSetup<BomberManAction, DoubleVector, BomberManRiskState>(
            policyId,
            trainablePredictor_risk,
            episodeDataMaker_risk,
            dataAggregator_risk
        );

        var metadataFactory = new RiskSearchMetadataFactory<BomberManAction, DoubleVector, BomberManRiskState>(actionClass, totalEntityCount);
        var searchNodeFactory = new SearchNodeBaseFactoryImpl<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(actionClass, metadataFactory);

        var totalRiskAllowedInference = riskAllowed;
        Supplier<Double> explorationSupplier = () -> 0.1;
        Supplier<Double> temperatureSupplier = () -> 1000.0;
        Supplier<Double> trainingRiskSupplier = () -> totalRiskAllowedInference;

        var treeUpdateConditionFactory = new FixedUpdateCountTreeConditionFactory(treeExpansionCount);

        var strategiesProvider = new StrategiesProvider<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(
            actionClass,
            InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW,
            InferenceNonExistingFlowStrategy.MAX_UCB_VALUE,
            ExplorationExistingFlowStrategy.SAMPLE_UCB_VALUE_WITH_TEMPERATURE,
            ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VALUE_WITH_TEMPERATURE,
//            FlowOptimizerType.HARD_HARD,
            FlowOptimizerType.HARD_HARD,
            SubTreeRiskCalculatorType.FLOW_SUM,
            NoiseStrategy.NOISY_05_06);

        var updater = new RalphTreeUpdater<BomberManAction, DoubleVector, BomberManRiskState>();
        var nodeEvaluator = maxBatchedDepth == 0 ?
            new RalphNodeEvaluator<BomberManAction, BomberManRiskState>(searchNodeFactory, trainablePredictor_risk, config.isModelKnown()) :
            new RalphBatchNodeEvaluator<BomberManAction, BomberManRiskState>(searchNodeFactory, trainablePredictor_risk, maxBatchedDepth, config.isModelKnown());


        var riskPolicy =  new PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState>(
            policyId,
            1,
            (initialState_, policyMode_, policyId_, random_) -> {
                Supplier<RalphNodeSelector<BomberManAction, DoubleVector, BomberManRiskState>> nodeSelectorSupplier = () -> new RalphNodeSelector<>(random_, config.isModelKnown(), cpuct, totalActionCount);
                var node = searchNodeFactory.createNode(initialState_, metadataFactory.createEmptyNodeMetadata(), new EnumMap<>(actionClass));
                switch(policyMode_) {
                    case INFERENCE:
                        return new RalphPolicy<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(
                            policyId_,
                            random_,
                            treeUpdateConditionFactory.create(),
                            new RiskAverseSearchTree<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(
                                searchNodeFactory,
                                node,
                                nodeSelectorSupplier.get(),
                                updater,
                                nodeEvaluator,
                                random_,
                                totalRiskAllowedInference,
                                riskDecay,
                                strategiesProvider));
                    case TRAINING:
                        return new RalphPolicy<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(
                            policyId_,
                            random_,
                            treeUpdateConditionFactory.create(),
                            new RiskAverseSearchTree<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(
                                searchNodeFactory,
                                node,
                                nodeSelectorSupplier.get(),
                                updater,
                                nodeEvaluator,
                                random_,
                                trainingRiskSupplier.get(),
                                riskDecay,
                                strategiesProvider),
                            explorationSupplier.get(),
                            temperatureSupplier.get());
                    default: throw EnumUtils.createExceptionForNotExpectedEnumValue(policyMode_);
                }
            },
            List.of(predictorTrainingSetup_risk)
        );

        System.out.println("Just for compiler purposes: " + riskPolicy.getCategoryId());
        return riskPolicy;
    }

    private static PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState> getAlphaZeroPlayer(int modelInputSize,
                                                                                                          int actionCount,
                                                                                                          BomberManConfig config,
                                                                                                          SystemConfig systemConfig,
                                                                                                          int policyId,
                                                                                                          int totalActionCount,
                                                                                                          double discountFactor,
                                                                                                          int treeExpansionCount,
                                                                                                          int totalEntityCount,
                                                                                                          int maxEvaluationDepth) throws IOException, InterruptedException {
        var alphaGoPolicySupplier = new AlphaZeroPolicyDefinitionSupplier<BomberManAction, BomberManRiskState>(BomberManAction.class, totalEntityCount, config);
        var path_ = Paths.get("PythonScripts", "tensorflow_models", "riskBomberManExample02", "create_alphazero_prototype.py");

        var tfModelAsBytes_ = TFHelper.loadTensorFlowModel(path_, systemConfig.getRandomSeed(),  modelInputSize, totalEntityCount, actionCount);
        var tfModel_ = new TFModelImproved(
            modelInputSize,
            totalEntityCount + actionCount,
            8192,
            1,
            0.8,
            0.01,
            tfModelAsBytes_,
            systemConfig.getParallelThreadsCount(),
            new SplittableRandom(systemConfig.getRandomSeed()));

        var trainablePredictorAlphaGoEval_1 = new TrainableApproximator(new TensorflowTrainablePredictor(tfModel_));
        var dataAggregatorAlphaGoEval_1 = new ReplayBufferDataAggregator(1000);
        var episodeDataMakerAlphaGoEval_1 = new AlphaZeroDataMaker_V1<BomberManAction, BomberManRiskState>(policyId, totalActionCount, discountFactor, dataAggregatorAlphaGoEval_1);
//        var dataAggregatorAlphaGoEval_1 = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetupAlphaGoEval_2 = new PredictorTrainingSetup<>(
            policyId,
            trainablePredictorAlphaGoEval_1,
            episodeDataMakerAlphaGoEval_1,
            dataAggregatorAlphaGoEval_1
        );

        return alphaGoPolicySupplier.getPolicyDefinition(policyId, 1, 1, () -> 0.1, treeExpansionCount, predictorTrainingSetupAlphaGoEval_2, maxEvaluationDepth);
    }

    private static PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState> getMctsPolicy(int inGameEntityCount, int modelInputSize, ProblemConfig problemConfig, SystemConfig systemConfig, int policyId, double discountFactor, int treeExpansionCount, double cpuct, int totalEntityCount, int maxEvaluationDepth) throws IOException, InterruptedException {
        var mctsPolicySupplier = new MCTSPolicyDefinitionSupplier<BomberManAction, BomberManRiskState>(BomberManAction.class, inGameEntityCount, problemConfig);
        var path_ = Paths.get("PythonScripts", "tensorflow_models", "riskBomberManExample02", "create_value_vectorized_model.py");

        var tfModelAsBytes_ = TFHelper.loadTensorFlowModel(path_, systemConfig.getRandomSeed(), modelInputSize, totalEntityCount, 0);
        var tfModel_ = new TFModelImproved(
            modelInputSize,
            totalEntityCount,
            8192,
            1,
            0.8,
            0.01,
            tfModelAsBytes_,
            systemConfig.getParallelThreadsCount(),
            new SplittableRandom(systemConfig.getRandomSeed()));

        var trainablePredictorMCTSEval_1 = new TrainableApproximator(new TensorflowTrainablePredictor(tfModel_));
        var dataAggregatorMCTSEval_1 = new ReplayBufferDataAggregator(1000);
        var episodeDataMakerMCTSEval_1 = new VectorValueDataMaker<BomberManAction, BomberManRiskState>(discountFactor, policyId, dataAggregatorMCTSEval_1);
//        var dataAggregatorMCTSEval_1 = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetupMCTSEval_1 = new PredictorTrainingSetup<>(
            policyId,
            trainablePredictorMCTSEval_1,
            episodeDataMakerMCTSEval_1,
            dataAggregatorMCTSEval_1
        );
        return mctsPolicySupplier.getPolicyDefinition(policyId, 1, () -> 0.1, cpuct, treeExpansionCount, predictorTrainingSetupMCTSEval_1, maxEvaluationDepth);
    }

    private static PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState> getValuePolicy(SystemConfig systemConfig, int policyId, double discountFactor, int modelInputSize) throws IOException, InterruptedException {
        var defaultPrediction_value = new double[] {0.0};
        var valuePolicySupplier = new ValuePolicyDefinitionSupplier<BomberManAction, BomberManRiskState>();

        var path = Paths.get("PythonScripts", "tensorflow_models", "riskBomberManExample02", "create_value_model.py");
        var tfModelAsBytes = TFHelper.loadTensorFlowModel(path, systemConfig.getRandomSeed(), modelInputSize, 1, 0);
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

        var trainablePredictor2 = new TrainableApproximator(new TensorflowTrainablePredictor(tfModel));
        var dataAggregator2 = new ReplayBufferDataAggregator(1000);
        var episodeDataMaker2 = new ValueDataMaker<BomberManAction, BomberManRiskState>(discountFactor, policyId, dataAggregator2);

        var predictorTrainingSetup2 = new PredictorTrainingSetup<>(
            policyId,
            trainablePredictor2,
            episodeDataMaker2,
            dataAggregator2
        );
        return valuePolicySupplier.getPolicyDefinition(policyId, 1, () -> 0.1, predictorTrainingSetup2);
    }

}
