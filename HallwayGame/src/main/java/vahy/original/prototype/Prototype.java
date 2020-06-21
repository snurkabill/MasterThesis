package vahy.original.prototype;

import vahy.api.experiment.ApproximatorConfigBuilder;
import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.StochasticStrategy;
import vahy.api.experiment.SystemConfig;
import vahy.api.experiment.SystemConfigBuilder;
import vahy.api.learning.ApproximatorType;
import vahy.api.learning.dataAggregator.DataAggregationAlgorithm;
import vahy.api.learning.trainer.EpisodeDataMaker;
import vahy.config.AlgorithmConfigBuilder;
import vahy.config.EvaluatorType;
import vahy.config.PaperAlgorithmConfig;
import vahy.config.SelectorType;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.predictor.DataTableDistributionPredictorWithLr;
import vahy.impl.runner.PolicyDefinition;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.original.environment.HallwayAction;
import vahy.original.environment.config.ConfigBuilder;
import vahy.original.environment.config.GameConfig;
import vahy.original.environment.state.HallwayStateImpl;
import vahy.original.environment.state.StateRepresentation;
import vahy.original.game.HallwayGameInitialInstanceSupplier;
import vahy.original.game.HallwayInstance;
import vahy.paperGenerics.PaperRound;
import vahy.paperGenerics.PaperTreeUpdater;
import vahy.paperGenerics.evaluator.PaperNodeEvaluator;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.metadata.PaperMetadataFactory;
import vahy.paperGenerics.policy.PaperPolicyRecord;
import vahy.paperGenerics.policy.PaperPolicySupplierImpl;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.linearProgram.NoiseStrategy;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.paperGenerics.reinforcement.learning.PaperEpisodeDataMaker_V1;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

public class Prototype {

    public static void main(String[] args) {

        var algorithmConfig = getAlgorithmConfig();
        var systemConfig = getSystemConfig();
        var problemConfig = getGameConfig();

        var commonAlgorithmConfig = new CommonAlgorithmConfig() {

            @Override
            public String toLog() {
                return "";
            }

            @Override
            public String toFile() {
                return "";
            }

            @Override
            public int getBatchEpisodeCount() {
                return 1000;
            }

            @Override
            public int getStageCount() {
                return 1000;
            }
        };

        EpisodeDataMaker<HallwayAction, DoubleVector, HallwayStateImpl, PaperPolicyRecord> dataMaker = new PaperEpisodeDataMaker_V1<>(1.0, 1);
        var predictor = new DataTableDistributionPredictorWithLr(new double[] {0.0, 0.0, 0.0, 0.0, 0.0}, 0.001);

        PredictorTrainingSetup<HallwayAction, DoubleVector, HallwayStateImpl, PaperPolicyRecord> predictorSetup = new PredictorTrainingSetup<HallwayAction, DoubleVector, HallwayStateImpl, PaperPolicyRecord>(
            1,
            predictor,
            dataMaker,
            new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>()));

        var metadataFactory = new PaperMetadataFactory<HallwayAction, DoubleVector, HallwayStateImpl>(HallwayAction.class);
        var paperTreeUpdater = new PaperTreeUpdater<HallwayAction, DoubleVector, HallwayStateImpl>();
        var actionClazz = HallwayAction.class;

        var strategiesProvider = new StrategiesProvider<HallwayAction, DoubleVector, PaperMetadata<HallwayAction>, HallwayStateImpl>(
            actionClazz,
            InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW,
            InferenceNonExistingFlowStrategy.MAX_UCB_VALUE,
            ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE,
            ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VALUE_WITH_TEMPERATURE,
            FlowOptimizerType.HARD_HARD,
            SubTreeRiskCalculatorType.FLOW_SUM,
            SubTreeRiskCalculatorType.PRIOR_SUM,
            NoiseStrategy.NOISY_05_06);

        PolicyDefinition<HallwayAction, DoubleVector, HallwayStateImpl, PaperPolicyRecord> policyDefinition = new PolicyDefinition<HallwayAction, DoubleVector, HallwayStateImpl, PaperPolicyRecord>(
            1,
            1,
            new PaperPolicySupplierImpl<HallwayAction, DoubleVector, PaperMetadata<HallwayAction>, HallwayStateImpl>(
                    actionClazz,
                    metadataFactory,
                    1.0,
                    null,
                    new PaperNodeEvaluator<>(
                        new SearchNodeBaseFactoryImpl<HallwayAction, DoubleVector, PaperMetadata<HallwayAction>, HallwayStateImpl>(HallwayAction.class, metadataFactory),
                        predictor,
                        null,
                        null,
                        HallwayAction.playerActions,
                        HallwayAction.environmentActions),
                    paperTreeUpdater,
                    new FixedUpdateCountTreeConditionFactory(100),
                    strategiesProvider,
                    () -> 0.5,
                    () -> 1.0,
                    () -> 1.0
                ),
            List.of(predictorSetup)
            );


        var roundBuilder = new PaperRound<GameConfig, HallwayAction, HallwayStateImpl>()
            .setSystemConfig(systemConfig)
            .setProblemConfig(problemConfig)
            .setCommonAlgorithmConfig(commonAlgorithmConfig)
            .setInitialStateSupplier(HallwayGameInitialInstanceSupplier::new)
            .setPolicyDefinitionList(List.of(policyDefinition));

        roundBuilder.execute();
    }

    private static GameConfig getGameConfig() {
        return new ConfigBuilder()
            .maximalStepCountBound(500)
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
            // REINFORCEMENT
            .discountFactor(1)
            .batchEpisodeCount(100)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(50))
            .stageCount(200)
            .evaluatorType(EvaluatorType.RALF)
//            .setBatchedEvaluationSize(1)

            .selectorType(SelectorType.UCB)
            .globalRiskAllowed(1.00)

            .setPlayerApproximatorConfig(new ApproximatorConfigBuilder().setApproximatorType(ApproximatorType.HASHMAP_LR).setDataAggregationAlgorithm(DataAggregationAlgorithm.EVERY_VISIT_MC).setLearningRate(0.1).build())

            .riskSupplier(new Supplier<Double>() {
                @Override
                public String toString() {
                    return "() -> 1.00";
                }

                @Override
                public Double get() {
                    return 1.00;
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
