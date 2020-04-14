package vahy.original.solutionExamples;

import vahy.api.experiment.ApproximatorConfigBuilder;
import vahy.api.learning.ApproximatorType;
import vahy.api.learning.dataAggregator.DataAggregationAlgorithm;
import vahy.config.AlgorithmConfigBuilder;
import vahy.config.PaperAlgorithmConfig;
import vahy.config.SelectorType;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.original.environment.config.ConfigBuilder;
import vahy.original.environment.config.GameConfig;
import vahy.original.environment.state.StateRepresentation;
import vahy.original.game.HallwayInstance;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;

import java.util.function.Supplier;

public class Benchmark16Solution extends DefaultLocalBenchmark {

    public static void main(String[] args) {
        var benchmark = new Benchmark16Solution();
        benchmark.runBenchmark();
    }

    @Override
    protected GameConfig createGameConfig() {
        return new ConfigBuilder()
            .maximalStepCountBound(1000)

            .reward(100)
            .noisyMoveProbability(0.1)
            .stepPenalty(4)
            .trapProbability(0.3)
            .stateRepresentation(StateRepresentation.COMPACT)
            .gameStringRepresentation(HallwayInstance.BENCHMARK_16)
            .buildConfig();
    }


    @Override
    protected PaperAlgorithmConfig createAlgorithmConfig() {
        int batchSize = 10;

        return new AlgorithmConfigBuilder()
            //MCTS
            .cpuctParameter(1)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(1))
            // REINFORCEMENTs
            .discountFactor(1)
            .batchEpisodeCount(batchSize)
            .stageCount(30000)

            .setPlayerApproximatorConfig(new ApproximatorConfigBuilder().setApproximatorType(ApproximatorType.HASHMAP_LR).setDataAggregationAlgorithm(DataAggregationAlgorithm.EVERY_VISIT_MC).setLearningRate(0.1).build())
            .selectorType(SelectorType.UCB)
            .globalRiskAllowed(1.0)
            .explorationConstantSupplier(new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    var x = Math.exp(-callCount / 1000000.0);
                    if(callCount % batchSize == 0) {
                        logger.info("Exploration constant: [{}] in call: [{}]", x, callCount);
                    }
                    return x;
//                    return 1.0;
                }
            })
            .temperatureSupplier(new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    double x = Math.exp(-callCount / 2000000.0) * 10;
                    if(callCount % batchSize == 0) {
                        logger.info("Temperature constant: [{}] in call: [{}]", x, callCount);
                    }
                    return x;
//                    return 1.5;
                }
            })
            .riskSupplier(() -> 1.0)
            .setInferenceExistingFlowStrategy(InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW)
            .setInferenceNonExistingFlowStrategy(InferenceNonExistingFlowStrategy.MAX_UCB_VISIT)
            .setExplorationExistingFlowStrategy(ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE)
            .setExplorationNonExistingFlowStrategy(ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VISIT)
            .setFlowOptimizerType(FlowOptimizerType.HARD_HARD)
            .setSubTreeRiskCalculatorTypeForKnownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY)
            .setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY)
            .buildAlgorithmConfig();
    }
}
