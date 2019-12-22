package vahy.original.solutionExamples;

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

public class Benchmark07Solution extends  DefaultLocalBenchmark {

    public static void main(String[] args) {
        var benchmark = new Benchmark07Solution();
        benchmark.runBenchmark();

    }

    @Override
    protected GameConfig createGameConfig() {
        return new ConfigBuilder()
            .reward(10_000)
            .noisyMoveProbability(0.4)
            .stepPenalty(1)
            .trapProbability(1)
            .stateRepresentation(StateRepresentation.COMPACT)
            .gameStringRepresentation(HallwayInstance.BENCHMARK_07)
            .buildConfig();
    }

    @Override
    protected PaperAlgorithmConfig createAlgorithmConfig() {
        return new AlgorithmConfigBuilder()
            //MCTS
            .cpuctParameter(3)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(100))
            //.mcRolloutCount(1)
            //NN
            .trainingBatchSize(0)
            .trainingEpochCount(0)
            // REINFORCEMENT
            .discountFactor(1)

            .batchEpisodeCount(50)
            .stageCount(1000)

            .maximalStepCountBound(1000)
            .trainerAlgorithm(DataAggregationAlgorithm.EVERY_VISIT_MC)

//            .approximatorType(ApproximatorType.HASHMAP)
            .approximatorType(ApproximatorType.HASHMAP_LR)
            .evaluatorType(EvaluatorType.RALF)
            .learningRate(0.01)

            .replayBufferSize(10000)
            .selectorType(SelectorType.UCB)
            .globalRiskAllowed(0.0)
            .riskSupplier(() -> 0.0)
            .explorationConstantSupplier(new Supplier<>() {
                private int callCount = 0;

                @Override
                public Double get() {
                    callCount++;
//                    return Math.exp(-callCount / 10000.0) ;
                    return 0.05;
                }
            })
            .temperatureSupplier(new Supplier<>() {
                private int callCount = 0;

                @Override
                public Double get() {
                    callCount++;
//                    return Math.exp(-callCount / 20000.0) * 5;
                    return 1.0;
                }
            })
            .setInferenceExistingFlowStrategy(InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW)
            .setInferenceNonExistingFlowStrategy(InferenceNonExistingFlowStrategy.MAX_UCB_VISIT)
            .setExplorationExistingFlowStrategy(ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE)
            .setExplorationNonExistingFlowStrategy(ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VISIT)
            .setFlowOptimizerType(FlowOptimizerType.HARD_HARD_SOFT)
            .setSubTreeRiskCalculatorTypeForKnownFlow(SubTreeRiskCalculatorType.FLOW_SUM)
            .setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY)
            .buildAlgorithmConfig();
    }
}
