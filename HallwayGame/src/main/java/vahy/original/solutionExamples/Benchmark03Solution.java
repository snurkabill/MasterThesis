package vahy.original.solutionExamples;

import vahy.api.experiment.ApproximatorConfigBuilder;
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

public class Benchmark03Solution extends DefaultLocalBenchmark {

    public static void main(String[] args) {
        var benchmark = new Benchmark03Solution();
        benchmark.runBenchmark();
    }

    @Override
    protected GameConfig createGameConfig() {
        return new ConfigBuilder()
            .maximalStepCountBound(1000)
            .reward(100)
            .noisyMoveProbability(0.0)
            .stepPenalty(10)
            .trapProbability(0.1)
            .stateRepresentation(StateRepresentation.COMPACT)
            .gameStringRepresentation(HallwayInstance.BENCHMARK_03)
            .buildConfig();
    }

    @Override
    protected PaperAlgorithmConfig createAlgorithmConfig() {
        return new AlgorithmConfigBuilder()
            //MCTS
            .cpuctParameter(3)

            //NN
            // REINFORCEMENT
            .discountFactor(1)
            .batchEpisodeCount(10)
            .setPlayerApproximatorConfig(new ApproximatorConfigBuilder().setApproximatorType(ApproximatorType.HASHMAP).setDataAggregationAlgorithm(DataAggregationAlgorithm.FIRST_VISIT_MC).build())

            .setBatchedEvaluationSize(1)

            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(25))
            .stageCount(50)
            .evaluatorType(EvaluatorType.RALF_BATCHED)
            .globalRiskAllowed(1.00)
            .riskSupplier(() -> 1.00)

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
                    return 2.0;
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
