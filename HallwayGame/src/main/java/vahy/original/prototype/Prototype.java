package vahy.original.prototype;

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
import vahy.original.environment.HallwayAction;
import vahy.original.environment.agent.policy.environment.PaperEnvironmentPolicy;
import vahy.original.environment.config.ConfigBuilder;
import vahy.original.environment.config.GameConfig;
import vahy.original.environment.state.StateRepresentation;
import vahy.original.game.HallwayGameInitialInstanceSupplier;
import vahy.original.game.HallwayInstance;
import vahy.paperGenerics.PaperExperimentEntryPoint;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;

import java.nio.file.Path;
import java.util.function.Supplier;

public class Prototype {

    public static void main(String[] args) {

        var algorithmConfig = getAlgorithmConfig();
        var systemConfig = getSystemConfig();
        var problemConfig = getGameConfig();

        PaperExperimentEntryPoint.createExperimentAndRun(
            HallwayAction.class,
            HallwayGameInitialInstanceSupplier::new,
//            PaperEnvironmentPolicy.class,
            splittableRandom -> (initialState, policyMode) -> new PaperEnvironmentPolicy(splittableRandom),
            algorithmConfig,
            systemConfig,
            problemConfig,
            Path.of("Results")

        );
    }

    private static GameConfig getGameConfig() {
        return new ConfigBuilder()
            .reward(100)
            .noisyMoveProbability(0.1)
            .stepPenalty(1)
            .trapProbability(1)
            .stateRepresentation(StateRepresentation.COMPACT)
            .gameStringRepresentation(HallwayInstance.BENCHMARK_05)
            .buildConfig();
    }

    private static SystemConfig getSystemConfig() {
        return new SystemConfigBuilder()
            .setRandomSeed(0)
            .setStochasticStrategy(StochasticStrategy.REPRODUCIBLE)
            .setDrawWindow(true)
            .setParallelThreadsCount(4)
            .setSingleThreadedEvaluation(false)
            .setEvalEpisodeCount(1000)
            .buildSystemConfig();
    }

    private static PaperAlgorithmConfig getAlgorithmConfig() {
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
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(50))
            .stageCount(200)
            .evaluatorType(EvaluatorType.RALF)
//            .setBatchedEvaluationSize(1)
            .maximalStepCountBound(500)
            .trainerAlgorithm(DataAggregationAlgorithm.EVERY_VISIT_MC)
            .replayBufferSize(100_000)
            .trainingBatchSize(1)
            .learningRate(0.01)

            .approximatorType(ApproximatorType.HASHMAP_LR)
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

            .replayBufferSize(10000)
            .selectorType(SelectorType.UCB)

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

                @Override
                public String toString() {
                    return "() -> 1.05";
                }
            })

            .setInferenceExistingFlowStrategy(InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW)
            .setInferenceNonExistingFlowStrategy(InferenceNonExistingFlowStrategy.MAX_UCB_VALUE)
            .setExplorationExistingFlowStrategy(ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE)
            .setExplorationNonExistingFlowStrategy(ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VALUE_WITH_TEMPERATURE)
            .setFlowOptimizerType(FlowOptimizerType.HARD_HARD)
            .setSubTreeRiskCalculatorTypeForKnownFlow(SubTreeRiskCalculatorType.FLOW_SUM)
            .setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY)
            .buildAlgorithmConfig();
    }

}
