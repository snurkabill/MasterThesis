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
import vahy.impl.benchmark.PolicyResults;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.KnownModelPolicySupplier;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.original.environment.HallwayAction;
import vahy.original.environment.config.ConfigBuilder;
import vahy.original.environment.config.GameConfig;
import vahy.original.environment.state.HallwayStateImpl;
import vahy.original.environment.state.StateRepresentation;
import vahy.original.game.HallwayGameInitialInstanceSupplier;
import vahy.original.game.HallwayInstance;
import vahy.paperGenerics.PaperExperimentBuilder;
import vahy.paperGenerics.benchmark.PaperEpisodeStatistics;
import vahy.paperGenerics.policy.PaperPolicyRecord;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.utils.ThirdPartBinaryUtils;

import java.util.List;
import java.util.function.Supplier;

public class DefaultLocalBenchmark {

    public static final Logger logger = LoggerFactory.getLogger(DefaultLocalBenchmark.class.getName());

    public void runBenchmark() {
        ThirdPartBinaryUtils.cleanUpNativeTempFiles();

        var algorithmConfig = createAlgorithmConfig();
        var systemConfig = createSystemConfig();
        var problemConfig = createGameConfig();

//        PaperExperimentEntryPoint.createExperimentAndRun(
//            HallwayAction.class,
//            HallwayGameInitialInstanceSupplier::new,
//            HallwayPolicySupplier.class,
//            Collections.singletonList(algorithmConfig),
//            systemConfig,
//            problemConfig,
//            Path.of("Results")
//        );

        var paperExperimentBuilder = new PaperExperimentBuilder<GameConfig, HallwayAction, HallwayStateImpl, HallwayStateImpl>()
            .setActionClass(HallwayAction.class)
            .setStateClass(HallwayStateImpl.class)
            .setSystemConfig(systemConfig)
            .setAlgorithmConfigList(List.of(algorithmConfig))
            .setProblemConfig(problemConfig)
            .setProblemInstanceInitializerSupplier(HallwayGameInitialInstanceSupplier::new)
            .setOpponentSupplier(KnownModelPolicySupplier::new);

        var results = paperExperimentBuilder.execute();

        for (PolicyResults<HallwayAction, DoubleVector, HallwayStateImpl, HallwayStateImpl, PaperPolicyRecord, PaperEpisodeStatistics> result : results) {
            logger.info("PolicyId: " + result.getPolicy().getPolicyId());
            logger.info("Results: " + result.getEpisodeStatistics().printToLog());
        }

    }

    protected PaperAlgorithmConfig createAlgorithmConfig() {
        return new AlgorithmConfigBuilder()

            .policyId("Base")
            //MCTS
            .cpuctParameter(1)

            // REINFORCEMENT
            .discountFactor(1)
            .batchEpisodeCount(100)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(50))
            .stageCount(100)
            .evaluatorType(EvaluatorType.RALF)
//            .setBatchedEvaluationSize(1)

            .setPlayerApproximatorConfig(new ApproximatorConfigBuilder().setApproximatorType(ApproximatorType.HASHMAP_LR).setDataAggregationAlgorithm(DataAggregationAlgorithm.FIRST_VISIT_MC).setLearningRate(0.1).build())

            .globalRiskAllowed(0.00)
            .riskSupplier(new Supplier<Double>() {
                @Override
                public Double get() {
                    return 0.00;
                }

                @Override
                public String toString() {
                    return "() -> 1.00";
                }
            })


            .selectorType(SelectorType.UCB)

            .explorationConstantSupplier(new Supplier<Double>() {
                @Override
                public Double get() {
                    return 0.2;
                }

                @Override
                public String toString() {
                    return "() -> 0.20";
                }
            })
            .temperatureSupplier(new Supplier<Double>() {
                @Override
                public Double get() {
                    return 1.50;
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

    protected PaperAlgorithmConfig createAlgorithmConfig2() {
        return new AlgorithmConfigBuilder()
            .policyId("Without LR")
            //MCTS
            .cpuctParameter(1)

            // REINFORCEMENT
            .discountFactor(1)
            .batchEpisodeCount(100)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(50))
            .stageCount(100)
            .evaluatorType(EvaluatorType.RALF)
//            .setBatchedEvaluationSize(1)

            .globalRiskAllowed(0.00)
            .riskSupplier(new Supplier<Double>() {
                @Override
                public Double get() {
                    return 0.00;
                }

                @Override
                public String toString() {
                    return "() -> 1.00";
                }
            })

            .selectorType(SelectorType.UCB)

            .explorationConstantSupplier(new Supplier<Double>() {
                @Override
                public Double get() {
                    return 0.2;
                }

                @Override
                public String toString() {
                    return "() -> 0.20";
                }
            })
            .temperatureSupplier(new Supplier<Double>() {
                @Override
                public Double get() {
                    return 1.50;
                }

                @Override
                public String toString() {
                    return "() -> 1.05";
                }
            })

//        temperatureSupplier(new Supplier<>() {
//            @Override
//            public Double get() {
//                callCount++;
//                return Math.exp(-callCount / 10000.0);
//            }
//            private int callCount = 0;
//        })

            .setInferenceExistingFlowStrategy(InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW)
            .setInferenceNonExistingFlowStrategy(InferenceNonExistingFlowStrategy.MAX_UCB_VALUE)
            .setExplorationExistingFlowStrategy(ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE)
            .setExplorationNonExistingFlowStrategy(ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VALUE_WITH_TEMPERATURE)
            .setFlowOptimizerType(FlowOptimizerType.HARD_HARD)
            .setSubTreeRiskCalculatorTypeForKnownFlow(SubTreeRiskCalculatorType.FLOW_SUM)
            .setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY)
            .buildAlgorithmConfig();
    }


    protected SystemConfig createSystemConfig() {
        return new SystemConfigBuilder()
            .setRandomSeed(0)
            .setStochasticStrategy(StochasticStrategy.REPRODUCIBLE)
            .setDrawWindow(false)
            .setParallelThreadsCount(Runtime.getRuntime().availableProcessors() - 1)
            .setSingleThreadedEvaluation(false)
            .setEvalEpisodeCount(1000)
            .setDumpTrainingData(false)
            .buildSystemConfig();
    }

    protected GameConfig createGameConfig() {
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

}
