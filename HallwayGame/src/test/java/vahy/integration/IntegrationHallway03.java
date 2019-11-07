package vahy.integration;

import org.testng.annotations.DataProvider;
import vahy.api.learning.dataAggregator.DataAggregationAlgorithm;
import vahy.config.AlgorithmConfigImpl;
import vahy.config.AlgorithmConfigBuilder;
import vahy.config.EvaluatorType;
import vahy.config.SelectorType;
import vahy.api.experiment.SystemConfig;
import vahy.environment.config.ConfigBuilder;
import vahy.environment.config.GameConfig;
import vahy.environment.state.StateRepresentation;
import vahy.game.HallwayInstance;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.api.learning.ApproximatorType;

public class IntegrationHallway03 extends  AbstractHallwayTest {

    @DataProvider(name = "TestDataProviderMethod")
    @Override
    public Object[][] experimentSettings() {
        return new Object[][] {
            {createExperiment_SAFE(), getSystemConfig(), createGameConfig(), HallwayInstance.BENCHMARK_03, 40, 0.0},
            {createExperiment_MIDDLE_RISK(), getSystemConfig(), createGameConfig(), HallwayInstance.BENCHMARK_03, 50.5, 0.05},
            {createExperiment_TOTAL_RISK(), getSystemConfig(), createGameConfig(), HallwayInstance.BENCHMARK_03, 61, 0.1}
        };
    }

    private SystemConfig getSystemConfig() {
        return new SystemConfig(0, false, Runtime.getRuntime().availableProcessors() - 1, false, 10_000, false);
    }

    public static GameConfig createGameConfig() {
        return new ConfigBuilder()
            .reward(100)
            .noisyMoveProbability(0.0)
            .stepPenalty(10)
            .trapProbability(0.1)
            .stateRepresentation(StateRepresentation.COMPACT)
            .buildConfig();
    }

    private static AlgorithmConfigBuilder genericAlgoConfig() {
        return new AlgorithmConfigBuilder()
            //MCTS
            .cpuctParameter(3)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(25))
            //NN
            .trainingBatchSize(0)
            .trainingEpochCount(0)
            // REINFORCEMENT
            .discountFactor(1)
            .batchEpisodeCount(100)
            .stageCount(10)
            .maximalStepCountBound(1000)
            .trainerAlgorithm(DataAggregationAlgorithm.EVERY_VISIT_MC)
            .approximatorType(ApproximatorType.HASHMAP)
            .selectorType(SelectorType.UCB)
            .evaluatorType(EvaluatorType.RALF_BATCHED)
            .globalRiskAllowed(0.00)
            .riskSupplier(() -> 0.00)
            .explorationConstantSupplier(() -> 0.2)
            .temperatureSupplier(() -> 2.0)
            .setInferenceExistingFlowStrategy(InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW)
            .setInferenceNonExistingFlowStrategy(InferenceNonExistingFlowStrategy.MAX_UCB_VISIT)
            .setExplorationExistingFlowStrategy(ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE)
            .setExplorationNonExistingFlowStrategy(ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VISIT)
            .setFlowOptimizerType(FlowOptimizerType.HARD_HARD)
            .setSubTreeRiskCalculatorTypeForKnownFlow(SubTreeRiskCalculatorType.FLOW_SUM)
            .setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType.PRIOR_SUM);
    }


    public static AlgorithmConfigImpl createExperiment_SAFE() {
        return genericAlgoConfig()
            .riskSupplier(() -> 0.0)
            .globalRiskAllowed(0.0)
            .buildAlgorithmConfig();
    }

    public static AlgorithmConfigImpl createExperiment_TOTAL_RISK() {
        return genericAlgoConfig()
            .riskSupplier(() -> 1.0)
            .globalRiskAllowed(1.0)
            .buildAlgorithmConfig();
    }

    public static AlgorithmConfigImpl createExperiment_MIDDLE_RISK() {
        return genericAlgoConfig()
            .riskSupplier(() -> 0.05)
            .globalRiskAllowed(0.05)
            .buildAlgorithmConfig();
    }

}
