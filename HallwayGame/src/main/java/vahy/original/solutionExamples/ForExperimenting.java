package vahy.original.solutionExamples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import vahy.original.environment.HallwayAction;
import vahy.original.environment.agent.policy.environment.HallwayPolicySupplier;
import vahy.original.environment.config.ConfigBuilder;
import vahy.original.environment.config.GameConfig;
import vahy.original.environment.state.HallwayStateImpl;
import vahy.original.environment.state.StateRepresentation;
import vahy.original.game.HallwayGameInitialInstanceSupplier;
import vahy.original.game.HallwayInstance;
import vahy.paperGenerics.PaperExperimentBuilder;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;

import java.util.List;
import java.util.function.Supplier;

public class ForExperimenting {

    public static final Logger logger = LoggerFactory.getLogger(ForExperimenting.class.getName());

    public static void main(String[] args) {

        var algorithmConfig = getAlgorithmConfig();
        var algorithmConfig2 = getAlgorithmConfig2();
        var systemConfig = getSystemConfig();
        var problemConfig = getGameConfig();

        var paperExperimentBuilder = new PaperExperimentBuilder<GameConfig, HallwayAction, HallwayStateImpl, HallwayStateImpl>()
            .setActionClass(HallwayAction.class)
            .setSystemConfig(systemConfig)
            .setAlgorithmConfigList(List.of(
                algorithmConfig
//                ,algorithmConfig2
            ))
            .setProblemConfig(problemConfig)
            .setOpponentSupplier(HallwayPolicySupplier::new)
            .setProblemInstanceInitializerSupplier(HallwayGameInitialInstanceSupplier::new);

        var results = paperExperimentBuilder.execute();

        logger.info(results.get(0).getEpisodeStatistics().printToLog());

    }

    private static GameConfig getGameConfig() {
        return new ConfigBuilder()
            .maximalStepCountBound(500)
            .isModelKnown(false)
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
            .setDumpTrainingData(false)
            .setDumpEvaluationData(false)
            .setEvalEpisodeCount(1000)
            .setEvalEpisodeCountDuringTraining(100)
            .setEvaluateDuringTraining(true)
            .setPythonVirtualEnvPath(System.getProperty("user.home") + "/.local/virtualenvs/tensorflow_2_0/bin/python")
            .buildSystemConfig();
    }

    private static PaperAlgorithmConfig getAlgorithmConfig() {

        var batchEpisodeSize = 50;

        return new AlgorithmConfigBuilder()
            //MCTS
            .policyId("Base")
            .cpuctParameter(1)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(30))

            //.mcRolloutCount(1)
            //NN
            .setPlayerApproximatorConfig(new ApproximatorConfigBuilder()
                .setApproximatorType(ApproximatorType.TF_NN)
                .setCreatingScriptName("create_model.py")
                .setDataAggregationAlgorithm(DataAggregationAlgorithm.REPLAY_BUFFER)
                .setReplayBufferSize(1_000)
                .setTrainingBatchSize(256)
                .setTrainingEpochCount(1)
                .setLearningRate(0.1).build())

            .setOpponentApproximatorConfig(new ApproximatorConfigBuilder()
                .setApproximatorType(ApproximatorType.HASHMAP_LR)
                .setDataAggregationAlgorithm(DataAggregationAlgorithm.FIRST_VISIT_MC)
                .setLearningRate(0.1).build())

            // REINFORCEMENT
            .discountFactor(1)
            .batchEpisodeCount(batchEpisodeSize)
            .stageCount(50)

            .evaluatorType(EvaluatorType.RALF)
//            .setBatchedEvaluationSize(2)

            .selectorType(SelectorType.UCB)
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
                    var x = Math.exp(-callCount / 10000.0);
                    if(callCount % batchEpisodeSize == 0) {
                        logger.info("Temperature: [" + x + "]");
                    }
                    return x;
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

    private static PaperAlgorithmConfig getAlgorithmConfig2() {
        var batchEpisodeSize = 50;

        return new AlgorithmConfigBuilder()
            //MCTS
            .cpuctParameter(1)
            .policyId("Base2")
            //.mcRolloutCount(1)
            //NN
            .setPlayerApproximatorConfig(new ApproximatorConfigBuilder().setApproximatorType(ApproximatorType.HASHMAP_LR).setDataAggregationAlgorithm(DataAggregationAlgorithm.EVERY_VISIT_MC).setLearningRate(0.1).build())
            // REINFORCEMENT
            .discountFactor(1)
            .batchEpisodeCount(batchEpisodeSize)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(20))
            .stageCount(50)

            .evaluatorType(EvaluatorType.RALF)
            .setBatchedEvaluationSize(2)

            .selectorType(SelectorType.UCB)
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
                    var x = Math.exp(-callCount / 10000.0);
                    if(callCount % batchEpisodeSize == 0) {
                        logger.info("Temperature: [" + x + "]");
                    }
                    return x;
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
