package vahy.example;

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
import vahy.domain.SHConfig;
import vahy.domain.SHConfigBuilder;
import vahy.domain.SHInstance;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;

import java.util.List;
import java.util.function.Supplier;

public class Example05 extends SHExperiment {

    public static void main(String[] args) {
        Example05 example = new Example05();
        example.runBenchmark();
    }

    @Override
    protected SHConfig createProblemConfig() {
        return new SHConfigBuilder()
            .maximalStepCountBound(500)
            .isModelKnown(true)
            .reward(100)
            .stepPenalty(1)
            .trapProbability(0.1)
            .gameStringRepresentation(SHInstance.BENCHMARK_05)
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
    protected List<PaperAlgorithmConfig> createAlgorithmConfigList() {
        return List.of(
            new AlgorithmConfigBuilder()
                .algorithmId("Base")
                //MCTS
                .cpuctParameter(1)
                // REINFORCEMENT
                .batchEpisodeCount(100).stageCount(1000).treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(0)).discountFactor(1)
                .setPlayerApproximatorConfig(new ApproximatorConfigBuilder().setDataAggregationAlgorithm(DataAggregationAlgorithm.FIRST_VISIT_MC).setApproximatorType(ApproximatorType.HASHMAP_LR).setLearningRate(0.1).build())
                .selectorType(SelectorType.UCB)
                .evaluatorType(EvaluatorType.RALF) //            .setBatchedEvaluationSize(1)
                .globalRiskAllowed(1.00)
                .riskSupplier(new Supplier<Double>() {
                    @Override
                    public Double get() {
                        return 1.00;
                    }

                    @Override
                    public String toString() {
                        return "() -> 1.00";
                    }
                })
                .explorationConstantSupplier(new Supplier<Double>() {
                    @Override
                    public Double get() {
                        return 1.0;
                    }

                    @Override
                    public String toString() {
                        return "() -> 0.20";
                    }
                })
                .temperatureSupplier(new Supplier<Double>() {
                    private int callCount = 0;
                    @Override
                    public Double get() {
                        callCount++;
                        return Math.exp(-callCount / 10000.0);
//                    return 2.00;
                    }
                })

                .setInferenceExistingFlowStrategy(InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW).setInferenceNonExistingFlowStrategy(InferenceNonExistingFlowStrategy.MAX_UCB_VALUE)
                .setExplorationExistingFlowStrategy(ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE).setExplorationNonExistingFlowStrategy(ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VALUE_WITH_TEMPERATURE)
                .setFlowOptimizerType(FlowOptimizerType.HARD_HARD)
                .setSubTreeRiskCalculatorTypeForKnownFlow(SubTreeRiskCalculatorType.FLOW_SUM).setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY)
                .buildAlgorithmConfig()
        );
    }

}
