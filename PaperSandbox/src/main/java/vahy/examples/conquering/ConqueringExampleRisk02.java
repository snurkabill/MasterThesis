package vahy.examples.conquering;

import vahy.RiskStateWrapper;
import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.SystemConfig;
import vahy.api.policy.PolicyMode;
import vahy.benchmark.RiskEpisodeStatistics;
import vahy.benchmark.RiskEpisodeStatisticsCalculator;
import vahy.examples.bomberman.BomberManAction;
import vahy.examples.bomberman.BomberManConfig;
import vahy.examples.bomberman.BomberManInstance;
import vahy.examples.bomberman.BomberManRiskInstanceInitializer;
import vahy.examples.bomberman.BomberManRiskState;
import vahy.impl.RoundBuilder;
import vahy.impl.benchmark.PolicyResults;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.episode.InvalidInstanceSetupException;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.model.observation.DoubleVector;
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
import vahy.utils.EnumUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConqueringExampleRisk02 {

    private ConqueringExampleRisk02() {}

    public static void main(String[] args) throws IOException, InterruptedException, InvalidInstanceSetupException {
        var config = new BomberManConfig(100, true, 100, 1, 0, 3, 3, 1, 2, 0.1, BomberManInstance.BM_01, PolicyShuffleStrategy.NO_SHUFFLE);
        var systemConfig = new SystemConfig(987567, false, 7, false, 10000, 200, false, false, false, Path.of("TEST_PATH"));

        var algorithmConfig = new CommonAlgorithmConfigBase(5000, 200);

        var environmentPolicyCount = config.getEnvironmentPolicyCount();

        var actionClass = BomberManAction.class;
        var totalActionCount = actionClass.getEnumConstants().length;
        var discountFactor = 1.0;
        var treeExpansionCount = 20;
        var cpuct = 1.0;

        var instance = new BomberManRiskInstanceInitializer(config, new SplittableRandom(0)).createInitialState(PolicyMode.TRAINING);
        int totalEntityCount = instance.getTotalEntityCount();
        var modelInputSize = instance.getInGameEntityObservation(5).getObservedVector().length;

        var evaluator_batch_size = 1;

        var riskPolicy_0 = getRiskPolicy(config, systemConfig, environmentPolicyCount + 0, actionClass, totalActionCount, discountFactor, treeExpansionCount, cpuct, totalEntityCount, modelInputSize, evaluator_batch_size, 0.5, 1.0);
        var riskPolicy_1 = getRiskPolicy(config, systemConfig, environmentPolicyCount + 1, actionClass, totalActionCount, discountFactor, treeExpansionCount, cpuct, totalEntityCount, modelInputSize, evaluator_batch_size, 0.5, 1.0);

        List<PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState>> policyArgumentsList = List.of(
            riskPolicy_0,
            riskPolicy_1
        );

        var roundBuilder = RoundBuilder.getRoundBuilder(
            "BomberManRisk01",
            config,
            systemConfig,
            algorithmConfig,
            policyArgumentsList,
            null,
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

        var defaultPrediction = new double[totalEntityCount * 2 + totalActionCount];
        for (int i = totalEntityCount * 2; i < defaultPrediction.length; i++) {
            defaultPrediction[i] = 1.0 / totalActionCount;
        }

        var trainablePredictor = new RalphDataTablePredictorWithLr(defaultPrediction, 0.01, totalActionCount, totalEntityCount);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
        var episodeDataMaker = new RalphEpisodeDataMaker_V2<BomberManAction, BomberManRiskState>(policyId, totalActionCount, discountFactor, dataAggregator);

        var predictorTrainingSetup = new PredictorTrainingSetup<BomberManAction, DoubleVector, BomberManRiskState>(
            policyId,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );

        var metadataFactory = new RiskSearchMetadataFactory<BomberManAction, DoubleVector, BomberManRiskState>(actionClass, totalEntityCount);
        var searchNodeFactory = new SearchNodeBaseFactoryImpl<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(actionClass, metadataFactory);

        var totalRiskAllowedInference = riskAllowed;
        Supplier<Double> explorationSupplier = () -> 0.05;
        Supplier<Double> temperatureSupplier = () -> 10.0;
        Supplier<Double> trainingRiskSupplier = () -> totalRiskAllowedInference;

        var treeUpdateConditionFactory = new FixedUpdateCountTreeConditionFactory(treeExpansionCount);

        var strategiesProvider = new StrategiesProvider<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(
            actionClass,
            InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW,
            InferenceNonExistingFlowStrategy.MAX_UCB_VALUE,
            ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE,
            ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VALUE_WITH_TEMPERATURE,
            FlowOptimizerType.HARD_HARD,
            SubTreeRiskCalculatorType.PRIOR_SUM,
            NoiseStrategy.NOISY_10_11);

        var updater = new RalphTreeUpdater<BomberManAction, DoubleVector, BomberManRiskState>();
        var nodeEvaluator = maxBatchedDepth == 0 ?
            new RalphNodeEvaluator<BomberManAction, BomberManRiskState>(searchNodeFactory, trainablePredictor, config.isModelKnown()) :
            new RalphBatchNodeEvaluator<BomberManAction, BomberManRiskState>(searchNodeFactory, trainablePredictor, maxBatchedDepth, config.isModelKnown());


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
            List.of(predictorTrainingSetup)
        );

        System.out.println("Just for compiler purposes: " + riskPolicy.getCategoryId());
        return riskPolicy;
    }


}
