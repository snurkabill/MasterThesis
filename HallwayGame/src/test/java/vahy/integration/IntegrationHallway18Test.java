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

import java.util.function.Supplier;

public class IntegrationHallway18Test extends AbstractHallwayTest {

    @DataProvider(name = "TestDataProviderMethod")
    @Override
    public Object[][] experimentSettings() {
        return new Object[][] {
            {createExperiment_SAFE(), getSystemConfig(), createGameConfig(), 1270.0, 0.0},
            {createExperiment_MIDDLE_RISK(), getSystemConfig(), createGameConfig(), 1270.0, 0.055},
            {createExperiment_TOTAL_RISK(), getSystemConfig(), createGameConfig(), 1290.0, 0.105}
        };
    }

    private SystemConfig getSystemConfig() {
        return new SystemConfig(1000, false, Runtime.getRuntime().availableProcessors() - 1, false, 1_000, false, false, null, null);
    }

    public static GameConfig createGameConfig() {
        return new ConfigBuilder()
            .maximalStepCountBound(500)
            .reward(100)
            .noisyMoveProbability(0.0)
            .stepPenalty(10)
            .trapProbability(0.05)
            .stateRepresentation(StateRepresentation.COMPACT)
            .gameStringRepresentation(HallwayInstance.BENCHMARK_18)
            .buildConfig();
    }

    private static AlgorithmConfigBuilder genericAlgoConfig() {
        int batchSize = 100;
        return  new AlgorithmConfigBuilder()
            //MCTS
            .cpuctParameter(1)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(50))
            //.mcRolloutCount(1)
            //NN
            .trainingBatchSize(64)
            .trainingEpochCount(100)
            .learningRate(0.1)
            // REINFORCEMENTs
            .discountFactor(1)
            .batchEpisodeCount(batchSize)
            .stageCount(200)

            .trainerAlgorithm(DataAggregationAlgorithm.EVERY_VISIT_MC)
            .approximatorType(ApproximatorType.HASHMAP_LR)
            .evaluatorType(EvaluatorType.RALF)
            .replayBufferSize(20000)
            .selectorType(SelectorType.UCB)
            .globalRiskAllowed(1.00)
            .riskSupplier(() -> 1.00)
            .explorationConstantSupplier(() -> 1.0)
            .temperatureSupplier(new Supplier<>() {
                @Override
                public Double get() {
                    callCount++;
                    var value = Math.exp(-callCount / 10000.0);
                    if(callCount % batchSize == 0) {
                        logger.info("Temperature [" + value + "]");
                    }
                    return value;
                }
                private int callCount = 0;
            })
            .setInferenceExistingFlowStrategy(InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW)
            .setInferenceNonExistingFlowStrategy(InferenceNonExistingFlowStrategy.MAX_UCB_VISIT)
            .setExplorationExistingFlowStrategy(ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE)
            .setExplorationNonExistingFlowStrategy(ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VISIT_WITH_TEMPERATURE)
            .setFlowOptimizerType(FlowOptimizerType.HARD_HARD)
            .setSubTreeRiskCalculatorTypeForKnownFlow(SubTreeRiskCalculatorType.FLOW_SUM)
            .setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY)
            .setNoiseStrategy(NoiseStrategy.NOISY_03_04);
    }


    public static PaperAlgorithmConfig createExperiment_SAFE() {
        return genericAlgoConfig()
            .riskSupplier(() -> 0.0)
            .globalRiskAllowed(0.0)
            .stageCount(5)
            .explorationConstantSupplier(new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    return Math.exp(-callCount / 100000.0) / 5;
                }
            })
            .temperatureSupplier(new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    return Math.exp(-callCount / 200000.0) * 10;
                }
            })
            .buildAlgorithmConfig();
    }

    public static PaperAlgorithmConfig createExperiment_TOTAL_RISK() {
        return genericAlgoConfig()
            .riskSupplier(() -> 1.0)
            .globalRiskAllowed(1.0)
            .stageCount(300)
            .explorationConstantSupplier(() -> 1.0)
            .temperatureSupplier(new Supplier<>() {
                @Override
                public Double get() {
                    callCount++;
                    return Math.exp(-callCount / 10000.0);
                }
                private int callCount = 0;
            })
            .buildAlgorithmConfig();
    }

    public static PaperAlgorithmConfig createExperiment_MIDDLE_RISK() {
        return genericAlgoConfig()
            .riskSupplier(() -> 0.05)
            .globalRiskAllowed(0.05)
            .stageCount(100)
//            .explorationConstantSupplier(new Supplier<>() {
//                private int callCount = 0;
//                @Override
//                public Double get() {
//                    callCount++;
//                    return Math.exp(-callCount / 100000.0) / 5;
//                }
//            })
//            .temperatureSupplier(new Supplier<>() {
//                private int callCount = 0;
//                @Override
//                public Double get() {
//                    callCount++;
//                    return Math.exp(-callCount / 200000.0) * 10;
//                }
//            })
            .explorationConstantSupplier(() -> 0.5)
            .temperatureSupplier(new Supplier<>() {
                @Override
                public Double get() {
                    callCount++;
                    return Math.exp(-callCount / 10000.0);
                }
                private int callCount = 0;
            })
            .buildAlgorithmConfig();
    }
}
