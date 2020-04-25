package vahy.original.solutionExamples;

import vahy.api.experiment.ApproximatorConfigBuilder;
import vahy.api.experiment.StochasticStrategy;
import vahy.api.experiment.SystemConfig;
import vahy.api.experiment.SystemConfigBuilder;
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

public class Benchmark13Solution extends DefaultLocalBenchmark {

    public static void main(String[] args) {
        var benchmark = new Benchmark13Solution();
        benchmark.runBenchmark();
    }

    @Override
    protected GameConfig createGameConfig() {
        return new ConfigBuilder()
            .maximalStepCountBound(500)
            .reward(100)
            .noisyMoveProbability(0.0)
            .stepPenalty(4)
            .trapProbability(0.1)
            .stateRepresentation(StateRepresentation.COMPACT)
            .gameStringRepresentation(HallwayInstance.BENCHMARK_13)
            .buildConfig();
    }

    @Override
    protected SystemConfig createSystemConfig() {
        return new SystemConfigBuilder()
            .setRandomSeed(0)
            .setStochasticStrategy(StochasticStrategy.REPRODUCIBLE)
            .setDrawWindow(true)
            .setParallelThreadsCount(Runtime.getRuntime().availableProcessors() - 1)
            .setSingleThreadedEvaluation(false)
            .setEvalEpisodeCount(1000)
            .setDumpTrainingData(false)
            .buildSystemConfig();
    }


    @Override
    protected PaperAlgorithmConfig createAlgorithmConfig() {
        int batchSize = 1000;
        double allowedRisk = 1.0;

        return new AlgorithmConfigBuilder()
            .policyId("Base")
            //MCTS
            .cpuctParameter(1)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(10))
            .discountFactor(1)
            .batchEpisodeCount(batchSize)
            .stageCount(3000)

            .setPlayerApproximatorConfig(new ApproximatorConfigBuilder().setApproximatorType(ApproximatorType.HASHMAP_LR).setDataAggregationAlgorithm(DataAggregationAlgorithm.FIRST_VISIT_MC).setLearningRate(0.1).build())

            .selectorType(SelectorType.UCB)
            .evaluatorType(EvaluatorType.RALF)

            .globalRiskAllowed(allowedRisk)
            .riskSupplier(() -> allowedRisk)
            .explorationConstantSupplier(() -> 0.5)
            .temperatureSupplier(new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    double x = Math.exp(-callCount / 400000.0);
                    if(callCount % batchSize == 0) {
                        logger.info("Temperature constant: [{}] in call: [{}]", x, callCount);
                    }
                    return x;
//                    return 1.5;
                }
            })

            .setInferenceExistingFlowStrategy(InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW)
            .setInferenceNonExistingFlowStrategy(InferenceNonExistingFlowStrategy.MAX_UCB_VALUE)
            .setExplorationExistingFlowStrategy(ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE)
            .setExplorationNonExistingFlowStrategy(ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VALUE_WITH_TEMPERATURE)
            .setFlowOptimizerType(FlowOptimizerType.HARD_HARD)
            .setSubTreeRiskCalculatorTypeForKnownFlow(SubTreeRiskCalculatorType.PRIOR_SUM)
            .setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY)
            .buildAlgorithmConfig();
    }

}
