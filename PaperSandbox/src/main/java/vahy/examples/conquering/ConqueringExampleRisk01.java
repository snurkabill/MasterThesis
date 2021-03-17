package vahy.examples.conquering;

import vahy.RiskStateWrapper;
import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.policy.PolicyMode;
import vahy.benchmark.RiskEpisodeStatistics;
import vahy.benchmark.RiskEpisodeStatisticsCalculator;
import vahy.examples.coqnuering.ConqueringRiskInitializer;
import vahy.examples.coqnuering.ConqueringRiskState;
import vahy.impl.RoundBuilder;
import vahy.impl.benchmark.PolicyResults;
import vahy.impl.episode.DataPointGeneratorGeneric;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.episode.InvalidInstanceSetupException;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.dataAggregator.ReplayBufferDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.ValueDataMaker;
import vahy.impl.learning.trainer.VectorValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.ValuePolicyDefinitionSupplier;
import vahy.impl.policy.mcts.MCTSPolicyDefinitionSupplier;
import vahy.impl.predictor.DataTablePredictorWithLr;
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
import vahy.ralph.reinforcement.RalphDataTablePredictorWithLr;
import vahy.ralph.reinforcement.learning.RalphEpisodeDataMaker_V2;
import vahy.ralph.selector.RalphNodeSelector;
import vahy.tensorflow.TFHelper;
import vahy.tensorflow.TFModelImproved;
import vahy.utils.EnumUtils;
import vahy.utils.StreamUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConqueringExampleRisk01 {

    private ConqueringExampleRisk01() {}

    private static PredictorTrainingSetup<ConqueringAction, DoubleVector, ConqueringRiskState> getRiskTrainingSetup(int playerId, int totalEntityCount, int totalActionCount) {
        double discountFactor = 1;

        var defaultPrediction = new double[totalEntityCount * 2 + totalActionCount];
        for (int i = totalEntityCount * 2; i < defaultPrediction.length; i++) {

            defaultPrediction[i] = 1.0 / totalActionCount;
        }

        var trainablePredictor = new RalphDataTablePredictorWithLr(defaultPrediction, 0.01, totalActionCount, totalEntityCount);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
        var episodeDataMaker = new RalphEpisodeDataMaker_V2<ConqueringAction, ConqueringRiskState>(playerId, totalActionCount, discountFactor, dataAggregator);

        var predictorTrainingSetup = new PredictorTrainingSetup<ConqueringAction, DoubleVector, ConqueringRiskState>(
            playerId,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );

        return predictorTrainingSetup;
    }

    private static PredictorTrainingSetup<ConqueringAction, DoubleVector, ConqueringRiskState> getValueTrainingSetup(int playerId) {
        double discountFactor = 1;

        var defaultPrediction = new double[] {0};
        var trainablePredictor = new DataTablePredictorWithLr(defaultPrediction, 0.01);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
        var episodeDataMaker = new ValueDataMaker<ConqueringAction, ConqueringRiskState>(discountFactor, playerId, dataAggregator);

        var predictorTrainingSetup = new PredictorTrainingSetup<ConqueringAction, DoubleVector, ConqueringRiskState>(
            playerId,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );

        return predictorTrainingSetup;
    }

    public static void main(String[] args) throws IOException, InvalidInstanceSetupException, InterruptedException {
        var config = new ConqueringConfig(50, PolicyShuffleStrategy.NO_SHUFFLE, 100, 0, 2, 4, 0.5);
        var systemConfig = new SystemConfig(987567, false, 7, true, 10000, 200, true, false, false, Path.of("TEST_PATH"));

        var algorithmConfig = new CommonAlgorithmConfigBase(500, 200);

        var environmentPolicyCount = config.getEnvironmentPolicyCount();

        var actionClass = ConqueringAction.class;
        var totalActionCount = actionClass.getEnumConstants().length;
        System.out.println("total action count: " + totalActionCount);
        var discountFactor = 1.0;
        var treeExpansionCount = 20;
        var cpuct = 1.0;

        var instance = new ConqueringRiskInitializer(config, new SplittableRandom(0)).createInitialState(PolicyMode.TRAINING);
        int totalEntityCount = instance.getTotalEntityCount();
        var modelInputSize = instance.getInGameEntityObservation(5).getObservedVector().length;

        var evaluator_batch_size = 1;

//        var randomizedPlayer_0 = new PolicyDefinition<ConqueringAction, DoubleVector, ConqueringRiskState>(
//            environmentPolicyCount + 0,
//            1,
//            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<ConqueringAction, DoubleVector, ConqueringRiskState>(random, environmentPolicyCount + 0),
//            new ArrayList<>());
//
//        var randomizedPlayer_1 = new PolicyDefinition<ConqueringAction, DoubleVector, ConqueringRiskState>(
//            environmentPolicyCount + 1,
//            1,
//            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<ConqueringAction, DoubleVector, ConqueringRiskState>(random, environmentPolicyCount + 1),
//            new ArrayList<>());
//



//        var mctsEvalPlayer_0 = getMctsPolicy(totalEntityCount, modelInputSize, config, systemConfig, environmentPolicyCount + 0, discountFactor, treeExpansionCount, cpuct, totalEntityCount, evaluator_batch_size);
//        var mctsEvalPlayer_1 = getMctsPolicy(totalEntityCount, modelInputSize, config, systemConfig, environmentPolicyCount + 1, discountFactor, treeExpansionCount, cpuct, totalEntityCount, evaluator_batch_size);
//
//        System.out.println(mctsEvalPlayer_0);
//        System.out.println(mctsEvalPlayer_1);

//        var valuePlayer_0 = getValuePolicy(systemConfig, environmentPolicyCount + 0, discountFactor, modelInputSize, totalEntityCount, totalActionCount);
//        var valuePlayer_1 = getValuePolicy(systemConfig, environmentPolicyCount + 1, discountFactor, modelInputSize, totalEntityCount, totalActionCount);
//
//        System.out.println(valuePlayer_0);
//        System.out.println(valuePlayer_1);


        var riskPolicy_0 = getRiskPolicy(config, systemConfig, environmentPolicyCount + 0, actionClass, totalActionCount, discountFactor, treeExpansionCount, cpuct, totalEntityCount, modelInputSize, evaluator_batch_size, 0.3, 0.9);
        var riskPolicy_1 = getRiskPolicy(config, systemConfig, environmentPolicyCount + 1, actionClass, totalActionCount, discountFactor, treeExpansionCount, cpuct, totalEntityCount, modelInputSize, evaluator_batch_size, 0.3, 0.9);
//
        System.out.println(riskPolicy_0);
        System.out.println(riskPolicy_1);

        List<PolicyDefinition<ConqueringAction, DoubleVector, ConqueringRiskState>> policyArgumentsList = List.of(
            riskPolicy_0,
//            valuePlayer_0,
            riskPolicy_1
//            valuePlayer_1
        );

        var additionalStatistics = new DataPointGeneratorGeneric<RiskEpisodeStatistics>("Risk Hit Ratio", x -> StreamUtils.labelWrapperFunction(x.getRiskHitRatio()));
        var additionalStatistics2 = new DataPointGeneratorGeneric<RiskEpisodeStatistics>("Exhausted Risk in Index avg", x -> StreamUtils.labelWrapperFunction(x.getRiskExhaustedIndexAverage()));
        var additionalStatistics3 = new DataPointGeneratorGeneric<RiskEpisodeStatistics>("Exhausted Risk in Index stdev", x -> StreamUtils.labelWrapperFunction(x.getRiskExhaustedIndexStdev()));
        var additionalStatistics4 = new DataPointGeneratorGeneric<RiskEpisodeStatistics>("At the end threshold avg", x -> StreamUtils.labelWrapperFunction(x.getRiskThresholdAtEndAverage()));
        var additionalStatistics5 = new DataPointGeneratorGeneric<RiskEpisodeStatistics>("At the end threshold stdev", x -> StreamUtils.labelWrapperFunction(x.getRiskThresholdAtEndStdev()));


//        var additionalStatistics6 = new DataPointGeneratorGeneric<PaperEpisodeStatistics>("Combined payoff", x -> Collections.singletonList(new LabelData("", x.getTotalPayoffAverage().stream().mapToDouble(y -> y).sum())));
//        var additionalStatistics7 = new DataPointGeneratorGeneric<PaperEpisodeStatistics>("RandomNoise", x -> Collections.singletonList(new LabelData("", new SplittableRandom().nextDouble())));

        var roundBuilder = RoundBuilder.getRoundBuilder(
            "ConqueringRisk01",
            config,
            systemConfig,
            algorithmConfig,
            policyArgumentsList,
            List.of(additionalStatistics, additionalStatistics2, additionalStatistics3, additionalStatistics4, additionalStatistics5),
            ConqueringRiskInitializer::new,
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

    private static void printProperty(PolicyResults<ConqueringAction, DoubleVector, ConqueringRiskState, RiskEpisodeStatistics> results, Function<RiskEpisodeStatistics, List<Double>> getter, String propertyName) {
        List<Double> values = getter.apply(results.getEvaluationStatistics());
        System.out.println("Property: [" + propertyName + "]");
        for (int i = 0; i < values.size(); i++) {
            System.out.println("Policy" + i + ": " + values.get(i));
        }
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------");
    }

    private static PolicyDefinition<ConqueringAction, DoubleVector, ConqueringRiskState> getRiskPolicy(ConqueringConfig config,
                                                                                                     SystemConfig systemConfig,
                                                                                                     int policyId,
                                                                                                     Class<ConqueringAction> actionClass,
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

        var path_ = Paths.get("PythonScripts", "tensorflow_models", "riskBomberManExample00", "create_risk_model.py");

//        var tfModelAsBytes_ = TFHelper.loadTensorFlowModel(path_, systemConfig.getRandomSeed(),  modelInputSize, totalEntityCount, totalActionCount);
//        var tfModel_ = new TFModelImproved(
//            modelInputSize,
//            totalEntityCount * 2 + totalActionCount,
//            512,
//            1,
//            0.8,
//            0.01,
//            tfModelAsBytes_,
//            systemConfig.getParallelThreadsCount(),
//            new SplittableRandom(systemConfig.getRandomSeed()));
//
//        var trainablePredictor_risk = new TrainableApproximator(new TensorflowTrainablePredictor(tfModel_));
//        var dataAggregator_risk = new ReplayBufferDataAggregator(1000);
//        var episodeDataMaker_risk = new RalphEpisodeDataMaker_V2<ConqueringAction, ConqueringRiskState>(policyId, totalActionCount, discountFactor, dataAggregator_risk);
////        var trainablePredictor_risk = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
////        var dataAggregator_risk = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
//
//        var predictorTrainingSetup_risk = new PredictorTrainingSetup<ConqueringAction, DoubleVector, ConqueringRiskState>(
//            policyId,
//            trainablePredictor_risk,
//            episodeDataMaker_risk,
//            dataAggregator_risk
//        );

        var predictorTrainingSetup_risk = getRiskTrainingSetup(policyId, totalEntityCount, totalActionCount);
        var trainablePredictor_risk = predictorTrainingSetup_risk.getTrainablePredictor();
        var metadataFactory = new RiskSearchMetadataFactory<ConqueringAction, DoubleVector, ConqueringRiskState>(actionClass, totalEntityCount);
        var searchNodeFactory = new SearchNodeBaseFactoryImpl<ConqueringAction, DoubleVector, RalphMetadata<ConqueringAction>, ConqueringRiskState>(actionClass, metadataFactory);

        var totalRiskAllowedInference = riskAllowed;
        Supplier<Double> explorationSupplier = () -> 0.05;
        Supplier<Double> temperatureSupplier = () -> 10.0;
        Supplier<Double> trainingRiskSupplier = () -> totalRiskAllowedInference;

        var treeUpdateConditionFactory = new FixedUpdateCountTreeConditionFactory(treeExpansionCount);

        var strategiesProvider = new StrategiesProvider<ConqueringAction, DoubleVector, RalphMetadata<ConqueringAction>, ConqueringRiskState>(
            actionClass,
            InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW,
            InferenceNonExistingFlowStrategy.MAX_UCB_VALUE,
            ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE,
//            ExplorationExistingFlowStrategy.SAMPLE_UCB_VALUE_WITH_TEMPERATURE,
            ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VALUE_WITH_TEMPERATURE,
            FlowOptimizerType.HARD_HARD,
            SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY,
            NoiseStrategy.NOISY_10_11);

        var updater = new RalphTreeUpdater<ConqueringAction, DoubleVector, ConqueringRiskState>();
        var nodeEvaluator = maxBatchedDepth == 0 ?
            new RalphNodeEvaluator<ConqueringAction, ConqueringRiskState>(searchNodeFactory, trainablePredictor_risk, config.isModelKnown()) :
            new RalphBatchNodeEvaluator<ConqueringAction, ConqueringRiskState>(searchNodeFactory, trainablePredictor_risk, maxBatchedDepth, config.isModelKnown());


        var riskPolicy =  new PolicyDefinition<ConqueringAction, DoubleVector, ConqueringRiskState>(
            policyId,
            1,
            (initialState_, policyMode_, policyId_, random_) -> {
                Supplier<RalphNodeSelector<ConqueringAction, DoubleVector, ConqueringRiskState>> nodeSelectorSupplier = () -> new RalphNodeSelector<>(random_, config.isModelKnown(), cpuct, totalActionCount);
                var node = searchNodeFactory.createNode(initialState_, metadataFactory.createEmptyNodeMetadata(), new EnumMap<>(actionClass));
                switch(policyMode_) {
                    case INFERENCE:
                        return new RalphPolicy<ConqueringAction, DoubleVector, RalphMetadata<ConqueringAction>, ConqueringRiskState>(
                            policyId_,
                            random_,
                            treeUpdateConditionFactory.create(),
                            new RiskAverseSearchTree<ConqueringAction, DoubleVector, RalphMetadata<ConqueringAction>, ConqueringRiskState>(
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
                        return new RalphPolicy<ConqueringAction, DoubleVector, RalphMetadata<ConqueringAction>, ConqueringRiskState>(
                            policyId_,
                            random_,
                            treeUpdateConditionFactory.create(),
                            new RiskAverseSearchTree<ConqueringAction, DoubleVector, RalphMetadata<ConqueringAction>, ConqueringRiskState>(
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

//    private static PolicyDefinition<ConqueringAction, DoubleVector, ConqueringRiskState> getAlphaZeroPlayer(int modelInputSize,
//                                                                                                          int actionCount,
//                                                                                                          ConqueringConfig config,
//                                                                                                          SystemConfig systemConfig,
//                                                                                                          int policyId,
//                                                                                                          int totalActionCount,
//                                                                                                          double discountFactor,
//                                                                                                          int treeExpansionCount,
//                                                                                                          int totalEntityCount,
//                                                                                                          int maxEvaluationDepth) throws IOException, InterruptedException {
//        var alphaGoPolicySupplier = new AlphaZeroPolicyDefinitionSupplier<ConqueringAction, ConqueringRiskState>(ConqueringAction.class, totalEntityCount, config);
//        var path_ = Paths.get("PythonScripts", "tensorflow_models", "riskBomberManExample00", "create_alphazero_prototype.py");
//
//        var tfModelAsBytes_ = TFHelper.loadTensorFlowModel(path_, systemConfig.getRandomSeed(),  modelInputSize, totalEntityCount, actionCount);
//        var tfModel_ = new TFModelImproved(
//            modelInputSize,
//            totalEntityCount + actionCount,
//            8192,
//            1,
//            0.8,
//            0.01,
//            tfModelAsBytes_,
//            systemConfig.getParallelThreadsCount(),
//            new SplittableRandom(systemConfig.getRandomSeed()));
//
//        var trainablePredictorAlphaGoEval_1 = new TrainableApproximator(new TensorflowTrainablePredictor(tfModel_));
//        var episodeDataMakerAlphaGoEval_1 = new AlphaZeroDataMaker_V1<ConqueringAction, ConqueringRiskState>(policyId, totalActionCount, discountFactor);
////        var dataAggregatorAlphaGoEval_1 = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
//        var dataAggregatorAlphaGoEval_1 = new ReplayBufferDataAggregator(1000);
//
//        var predictorTrainingSetupAlphaGoEval_2 = new PredictorTrainingSetup<>(
//            policyId,
//            trainablePredictorAlphaGoEval_1,
//            episodeDataMakerAlphaGoEval_1,
//            dataAggregatorAlphaGoEval_1
//        );
//
//        return alphaGoPolicySupplier.getPolicyDefinition(policyId, 1, 1, () -> 0.1, treeExpansionCount, predictorTrainingSetupAlphaGoEval_2, maxEvaluationDepth);
//    }

    private static PolicyDefinition<ConqueringAction, DoubleVector, ConqueringRiskState> getMctsPolicy(int inGameEntityCount, int modelInputSize, ProblemConfig problemConfig, SystemConfig systemConfig, int policyId, double discountFactor, int treeExpansionCount, double cpuct, int totalEntityCount, int maxEvaluationDepth) throws IOException, InterruptedException {
        var mctsPolicySupplier = new MCTSPolicyDefinitionSupplier<ConqueringAction, ConqueringRiskState>(ConqueringAction.class, inGameEntityCount, problemConfig);
        var path_ = Paths.get("PythonScripts", "tensorflow_models", "riskBomberManExample00", "create_value_vectorized_model.py");

        var tfModelAsBytes_ = TFHelper.loadTensorFlowModel(path_, systemConfig.getRandomSeed(), modelInputSize, totalEntityCount, 0);
        var tfModel_ = new TFModelImproved(
            modelInputSize,
            totalEntityCount,
            512,
            1,
            0.8,
            0.01,
            tfModelAsBytes_,
            systemConfig.getParallelThreadsCount(),
            new SplittableRandom(systemConfig.getRandomSeed()));

        var trainablePredictorMCTSEval_1 = new TrainableApproximator(new TensorflowTrainablePredictor(tfModel_));
        var dataAggregatorMCTSEval_1 = new ReplayBufferDataAggregator(1000);
        var episodeDataMakerMCTSEval_1 = new VectorValueDataMaker<ConqueringAction, ConqueringRiskState>(discountFactor, policyId, dataAggregatorMCTSEval_1);
//        var dataAggregatorMCTSEval_1 = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetupMCTSEval_1 = new PredictorTrainingSetup<>(
            policyId,
            trainablePredictorMCTSEval_1,
            episodeDataMakerMCTSEval_1,
            dataAggregatorMCTSEval_1
        );
        return mctsPolicySupplier.getPolicyDefinition(policyId, 1, () -> 0.05, cpuct, treeExpansionCount, predictorTrainingSetupMCTSEval_1, maxEvaluationDepth);
    }

    private static PolicyDefinition<ConqueringAction, DoubleVector, ConqueringRiskState> getValuePolicy(SystemConfig systemConfig, int policyId, double discountFactor, int modelInputSize, int totalEntityCount, int totalActionCount) throws IOException, InterruptedException {
//        var defaultPrediction_value = new double[] {0.0};
        var valuePolicySupplier = new ValuePolicyDefinitionSupplier<ConqueringAction, ConqueringRiskState>();
//
//        var path = Paths.get("PythonScripts", "tensorflow_models", "riskBomberManExample00", "create_value_model.py");
//        var tfModelAsBytes = TFHelper.loadTensorFlowModel(path, systemConfig.getRandomSeed(), modelInputSize, 1, 0);
//        var tfModel = new TFModelImproved(
//            modelInputSize,
//            defaultPrediction_value.length,
//            512,
//            1,
//            0.8,
//            0.01,
//            tfModelAsBytes,
//            systemConfig.getParallelThreadsCount(),
//            new SplittableRandom(systemConfig.getRandomSeed()));
//
//        var trainablePredictor2 = new TrainableApproximator(new TensorflowTrainablePredictor(tfModel));
//        var dataAggregator2 = new ReplayBufferDataAggregator(1000);
//        var episodeDataMaker2 = new ValueDataMaker<ConqueringAction, ConqueringRiskState>(discountFactor, policyId, dataAggregator2);
//
//        var predictorTrainingSetup2 = new PredictorTrainingSetup<>(
//            policyId,
//            trainablePredictor2,
//            episodeDataMaker2,
//            dataAggregator2
//        );


//        var predictorTrainingSetup2 = getRiskTrainingSetup(policyId, totalEntityCount, totalActionCount);
        var predictorTrainingSetup2 = getValueTrainingSetup(policyId);
        return valuePolicySupplier.getPolicyDefinition(policyId, 1, () -> 0.1, predictorTrainingSetup2);
    }


}
