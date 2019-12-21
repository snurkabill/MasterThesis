package vahy.integration;

import org.testng.annotations.DataProvider;
import vahy.api.experiment.SystemConfig;
import vahy.api.learning.ApproximatorType;
import vahy.api.learning.dataAggregator.DataAggregationAlgorithm;
import vahy.config.AlgorithmConfigBuilder;
import vahy.config.EvaluatorType;
import vahy.config.PaperAlgorithmConfig;
import vahy.config.SelectorType;
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

import java.util.function.Supplier;

public class IntegrationHallway05Test extends AbstractHallwayTest {

    @DataProvider(name = "TestDataProviderMethod")
    @Override
    public Object[][] experimentSettings() {
        return new Object[][] {
            {createExperiment_SAFE(), getSystemConfig(), createGameConfig(), 280.0, 0.0},
            {createExperiment_MIDDLE_RISK(), getSystemConfig(), createGameConfig(),  278.0, 0.010},
            {createExperiment_TOTAL_RISK(), getSystemConfig(), createGameConfig(), 270.000, 0.050}
        };
    }

    private SystemConfig getSystemConfig() {
        return new SystemConfig(0, false, Runtime.getRuntime().availableProcessors() - 1, false, 1_000, false);
    }


    public static GameConfig createGameConfig() {
        return new ConfigBuilder()
            .reward(100)
            .noisyMoveProbability(0.1)
            .stepPenalty(1)
            .trapProbability(1)
            .stateRepresentation(StateRepresentation.COMPACT)
            .gameStringRepresentation(HallwayInstance.BENCHMARK_05)
            .buildConfig();
    }

    private static AlgorithmConfigBuilder genericAlgoConfig() {
        return new AlgorithmConfigBuilder()
            //MCTS
            .cpuctParameter(1)

            //.mcRolloutCount(1)
            //NN
            .trainingBatchSize(1)
            .trainingEpochCount(10)
            // REINFORCEMENT
            .discountFactor(1)

            .batchEpisodeCount(100)

            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(100))
            .stageCount(100)
            .evaluatorType(EvaluatorType.RALF)

            .maximalStepCountBound(1000)
            .trainerAlgorithm(DataAggregationAlgorithm.EVERY_VISIT_MC)
            .approximatorType(ApproximatorType.HASHMAP_LR)
            .globalRiskAllowed(1.0)
            .riskSupplier(() -> 1.0)

            .learningRate(0.1)
            .replayBufferSize(10000)
            .selectorType(SelectorType.UCB)

            .explorationConstantSupplier(new Supplier<>() {
                @Override
                public Double get() {
                    return 0.2;
                }
            })
            .temperatureSupplier(new Supplier<>() {
                @Override
                public Double get() {
                    return 1.5;
                }
            })

            .setInferenceExistingFlowStrategy(InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW)
            .setInferenceNonExistingFlowStrategy(InferenceNonExistingFlowStrategy.MAX_UCB_VISIT)
            .setExplorationExistingFlowStrategy(ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE)
            .setExplorationNonExistingFlowStrategy(ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VISIT)
            .setFlowOptimizerType(FlowOptimizerType.HARD_HARD)
            .setSubTreeRiskCalculatorTypeForKnownFlow(SubTreeRiskCalculatorType.FLOW_SUM)
            .setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY);
    }


    public static PaperAlgorithmConfig createExperiment_SAFE() {
        return genericAlgoConfig()
            .riskSupplier(() -> 0.0)
            .globalRiskAllowed(0.0)
//            .stageCount(50)
            .buildAlgorithmConfig();
    }

    public static PaperAlgorithmConfig createExperiment_TOTAL_RISK() {
        return genericAlgoConfig()
            .riskSupplier(() -> 1.0)
            .globalRiskAllowed(1.0)
            .stageCount(100)
            .buildAlgorithmConfig();
    }

    public static PaperAlgorithmConfig createExperiment_MIDDLE_RISK() {
        return genericAlgoConfig()
            .riskSupplier(() -> 0.05)
            .globalRiskAllowed(0.05)
//            .stageCount(200)
            .buildAlgorithmConfig();
    }
}
