package vahy.integration;

import org.testng.annotations.DataProvider;
import vahy.api.experiment.ApproximatorConfigBuilder;
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

import java.util.function.Supplier;

public class IntegrationHallway05Test extends AbstractHallwayTest {

    @DataProvider(name = "TestDataProviderMethod")
    @Override
    public Object[][] experimentSettings() {
        return new Object[][] {
            {createExperiment_SAFE(), getSystemConfig(), createGameConfig(), 280.0, 0.0},
            {createExperiment_MIDDLE_RISK(), getSystemConfig(), createGameConfig(),  280.0, 0.010},
            {createExperiment_TOTAL_RISK(), getSystemConfig(), createGameConfig(), 280.000, 0.050}
        };
    }

    private SystemConfig getSystemConfig() {
        return new SystemConfig(0, false, Runtime.getRuntime().availableProcessors() - 1, false, 10_000, 0, false, false, false, null, null);
    }

    public static GameConfig createGameConfig() {
        return new ConfigBuilder()
            .maximalStepCountBound(500)
            .isModelKnown(true)
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
            .algorithmId("Base")
            //MCTS
            .cpuctParameter(1)

            //.mcRolloutCount(1)
            .setPlayerApproximatorConfig(new ApproximatorConfigBuilder().setDataAggregationAlgorithm(DataAggregationAlgorithm.EVERY_VISIT_MC).setApproximatorType(ApproximatorType.HASHMAP_LR).setLearningRate(0.1).build())
            // REINFORCEMENT
            .discountFactor(1)

            .batchEpisodeCount(100)

            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(50))
            .stageCount(50)
            .evaluatorType(EvaluatorType.RALF)

            .globalRiskAllowed(1.0)
            .riskSupplier(() -> 1.0)

            .selectorType(SelectorType.UCB)

            .explorationConstantSupplier(new Supplier<>() {
                @Override
                public Double get() {
                    return 0.5;
                }
            })
            .temperatureSupplier(new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    return Math.exp(-callCount / 10000.0);
                }
            })

            .setInferenceExistingFlowStrategy(InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW)
            .setInferenceNonExistingFlowStrategy(InferenceNonExistingFlowStrategy.MAX_UCB_VALUE)
            .setExplorationExistingFlowStrategy(ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE)
            .setExplorationNonExistingFlowStrategy(ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VALUE_WITH_TEMPERATURE)
            .setFlowOptimizerType(FlowOptimizerType.HARD_HARD)
            .setSubTreeRiskCalculatorTypeForKnownFlow(SubTreeRiskCalculatorType.FLOW_SUM)
            .setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY)
            .setNoiseStrategy(NoiseStrategy.NOISY_03_04);
    }


    public static PaperAlgorithmConfig createExperiment_SAFE() {
        return genericAlgoConfig()
            .riskSupplier(() -> 0.0)
            .globalRiskAllowed(0.0)
            .stageCount(30)
            .buildAlgorithmConfig();
    }

    public static PaperAlgorithmConfig createExperiment_TOTAL_RISK() {
        return genericAlgoConfig()
            .riskSupplier(() -> 1.0)
            .globalRiskAllowed(1.0)
            .stageCount(200)
            .setPlayerApproximatorConfig(new ApproximatorConfigBuilder().setDataAggregationAlgorithm(DataAggregationAlgorithm.EVERY_VISIT_MC).setApproximatorType(ApproximatorType.HASHMAP_LR).setLearningRate(0.5).build())
            .temperatureSupplier(new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    return Math.exp(-callCount / 8000.0);
                }
            })
            .explorationConstantSupplier(new Supplier<>() {
                @Override
                public Double get() {
                    return 1.0;
                }
            })
            .buildAlgorithmConfig();
    }

    public static PaperAlgorithmConfig createExperiment_MIDDLE_RISK() {
        return genericAlgoConfig()
            .riskSupplier(() -> 0.05)
            .globalRiskAllowed(0.05)
            .stageCount(100)
            .buildAlgorithmConfig();
    }
}
