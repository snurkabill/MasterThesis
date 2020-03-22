package vahy.integration;

import org.testng.annotations.DataProvider;
import vahy.api.experiment.SystemConfig;
import vahy.api.learning.ApproximatorType;
import vahy.api.learning.dataAggregator.DataAggregationAlgorithm;
import vahy.config.AlgorithmConfigBuilder;
import vahy.config.EvaluatorType;
import vahy.config.PaperAlgorithmConfig;
import vahy.config.SelectorType;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.original.environment.config.ConfigBuilder;
import vahy.original.environment.config.GameConfig;
import vahy.original.environment.state.StateRepresentation;
import vahy.original.game.HallwayInstance;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.linearProgram.NoiseStrategy;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;

public class IntegrationHallway03Test extends AbstractHallwayTest {

    @DataProvider(name = "TestDataProviderMethod")
    @Override
    public Object[][] experimentSettings() {
        return new Object[][] {
            {createExperiment_SAFE(), getSystemConfig(), createGameConfig(), 40, 0.0},
            {createExperiment_MIDDLE_RISK(), getSystemConfig(), createGameConfig(), 50.4, 0.055},
            {createExperiment_TOTAL_RISK(), getSystemConfig(), createGameConfig(), 61, 0.1}
        };
    }

    private SystemConfig getSystemConfig() {
        return new SystemConfig(0, false, Runtime.getRuntime().availableProcessors() - 1, false, 10_000, false, false, null);
    }

    public static GameConfig createGameConfig() {
        return new ConfigBuilder()
            .reward(100)
            .noisyMoveProbability(0.0)
            .stepPenalty(10)
            .trapProbability(0.1)
            .stateRepresentation(StateRepresentation.COMPACT)
            .gameStringRepresentation(HallwayInstance.BENCHMARK_03)
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
            .stageCount(20)
            .maximalStepCountBound(1000)
            .trainerAlgorithm(DataAggregationAlgorithm.EVERY_VISIT_MC)
            .approximatorType(ApproximatorType.HASHMAP)
            .setBatchedEvaluationSize(1)
            .selectorType(SelectorType.UCB)
            .evaluatorType(EvaluatorType.RALF)
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
            .setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY)
            .setNoiseStrategy(NoiseStrategy.NOISY_03_04);
    }


    public static PaperAlgorithmConfig createExperiment_SAFE() {
        return genericAlgoConfig()
            .riskSupplier(() -> 0.0)
            .globalRiskAllowed(0.0)
            .buildAlgorithmConfig();
    }

    public static PaperAlgorithmConfig createExperiment_TOTAL_RISK() {
        return genericAlgoConfig()
            .riskSupplier(() -> 1.0)
            .globalRiskAllowed(1.0)
            .buildAlgorithmConfig();
    }

    public static PaperAlgorithmConfig createExperiment_MIDDLE_RISK() {
        return genericAlgoConfig()
            .riskSupplier(() -> 0.05)
            .globalRiskAllowed(0.05)
            .buildAlgorithmConfig();
    }

}
