package vahy.resignation.solutionExamples;

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
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;

import java.util.function.Supplier;

public class TestingRAMCP extends DefaultLocalBenchmark {

    public static void main(String[] args) {
        var benchmark = new Benchmark17Solution();
        benchmark.runBenchmark();
    }

    @Override
    protected GameConfig createGameConfig() {
        return new ConfigBuilder()
            .reward(100)
            .noisyMoveProbability(0.1)
            .stepPenalty(1)
            .trapProbability(1)
            .stateRepresentation(StateRepresentation.COMPACT)
            .gameStringRepresentation(HallwayInstance.BENCHMARK_05)
            .buildConfig();
    }

    @Override
    protected PaperAlgorithmConfig createAlgorithmConfig() {
        return  new AlgorithmConfigBuilder()
            //MCTS
            .cpuctParameter(3)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(100))
            //.mcRolloutCount(1)
            //NN
            .trainingBatchSize(1)
            .trainingEpochCount(10)
            // REINFORCEMENT
            .discountFactor(1)

            .batchEpisodeCount(100)
            .stageCount(100)

            .maximalStepCountBound(1000)
            .trainerAlgorithm(DataAggregationAlgorithm.EVERY_VISIT_MC)
            .approximatorType(ApproximatorType.HASHMAP_LR)
            .evaluatorType(EvaluatorType.RAMCP)
            .learningRate(0.1)

            .replayBufferSize(10000)
            .selectorType(SelectorType.UCB)

            .globalRiskAllowed(0.10)
            .riskSupplier(() -> 0.10)
            .explorationConstantSupplier(new Supplier<>() {
                @Override
                public Double get() {
                    return 0.0;
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
            .setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY)
            .buildAlgorithmConfig();
    }
}
