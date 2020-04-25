package vahy.integration;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import vahy.api.experiment.ApproximatorConfigBuilder;
import vahy.api.experiment.StochasticStrategy;
import vahy.api.experiment.SystemConfig;
import vahy.api.experiment.SystemConfigBuilder;
import vahy.api.learning.ApproximatorType;
import vahy.api.learning.dataAggregator.DataAggregationAlgorithm;
import vahy.config.AlgorithmConfigBuilder;
import vahy.config.EvaluatorType;
import vahy.config.SelectorType;
import vahy.impl.benchmark.PolicyResults;
import vahy.impl.model.observation.DoubleVector;
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
import vahy.paperGenerics.benchmark.PaperEpisodeStatistics;
import vahy.paperGenerics.policy.PaperPolicyRecord;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.utils.ThirdPartBinaryUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public class SingleVsBatchedEvaluatorTest {

    @BeforeTest
    public static void cleanUpNativeLibraries() {
        ThirdPartBinaryUtils.cleanUpNativeTempFiles();
    }

    private void assertResults(List<PolicyResults<HallwayAction, DoubleVector, HallwayStateImpl, HallwayStateImpl, PaperPolicyRecord, PaperEpisodeStatistics>> results) {
        for (var entry : results) {
            for (int i = 0; i < entry.getTrainingStatisticsList().size(); i++) {
                var record_0 = results.get(0).getTrainingStatisticsList().get(i);
                var record_1 = entry.getTrainingStatisticsList().get(i);
                Assert.assertEquals(record_0.getTotalPayoffAverage(), record_1.getTotalPayoffAverage(), Math.pow(10, -10));
                Assert.assertEquals(record_0.getTotalPayoffStdev(),   record_1.getTotalPayoffStdev(),   Math.pow(10, -10));
                Assert.assertEquals(record_0.getRiskHitCounter(),     record_1.getRiskHitCounter());
            }
        }
    }

    @Test
    public void SingleVsBatchedEvaluatorTest() {

        var algorithmConfig_0 = getAlgorithmConfig();
        var algorithmConfig_1 = getAlgorithmConfig();
        algorithmConfig_1.policyId("Batched_0");
        algorithmConfig_1.setBatchedEvaluationSize(0);
        algorithmConfig_1.evaluatorType(EvaluatorType.RALF_BATCHED);
        var algorithmConfig_2 = getAlgorithmConfig();
        algorithmConfig_2.policyId("Batched_1");
        algorithmConfig_2.setBatchedEvaluationSize(1);
        algorithmConfig_2.evaluatorType(EvaluatorType.RALF_BATCHED);

        var algorithmConfig_3 = getAlgorithmConfig();
        algorithmConfig_3.policyId("Batched_2");
        algorithmConfig_3.setBatchedEvaluationSize(2);
        algorithmConfig_3.evaluatorType(EvaluatorType.RALF_BATCHED);

        var systemConfig = getSystemConfig();
        var problemConfig = getGameConfig();

        var paperExperimentBuilder = new PaperExperimentBuilder<GameConfig, HallwayAction, HallwayStateImpl, HallwayStateImpl>()
            .setActionClass(HallwayAction.class)
            .setStateClass(HallwayStateImpl.class)
            .setSystemConfig(systemConfig)
            .setAlgorithmConfigList(List.of(
                algorithmConfig_0.buildAlgorithmConfig(),
                algorithmConfig_1.buildAlgorithmConfig(),
                algorithmConfig_2.buildAlgorithmConfig(),
                algorithmConfig_3.buildAlgorithmConfig()
            ))
            .setProblemConfig(problemConfig)
            .setOpponentSupplier(HallwayPolicySupplier::new)
            .setProblemInstanceInitializerSupplier(HallwayGameInitialInstanceSupplier::new);

        var results = paperExperimentBuilder.execute();
        assertResults(results);
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
            .isModelKnown(false)
            .buildConfig();
    }

    private static SystemConfig getSystemConfig() {
        return new SystemConfigBuilder()
            .setRandomSeed(0)
            .setStochasticStrategy(StochasticStrategy.REPRODUCIBLE)
            .setDrawWindow(false)
            .setParallelThreadsCount(Runtime.getRuntime().availableProcessors() - 1)
            .setSingleThreadedEvaluation(false)
            .setDumpTrainingData(false)
            .setDumpEvaluationData(false)
            .setEvalEpisodeCount(10)
            .setEvaluateDuringTraining(false)
            .setEvalEpisodeCountDuringTraining(1000)
            .setPythonVirtualEnvPath(System.getProperty("user.home") + "/.local/virtualenvs/tensorflow_2_0/bin/python")
            .setDumpPath(Path.of("Example05"))
            .buildSystemConfig();
    }

    private static AlgorithmConfigBuilder getAlgorithmConfig() {

        var batchEpisodeSize = 10;

        return new AlgorithmConfigBuilder()
            //MCTS
            .cpuctParameter(1)
            .policyId("Base")

            .setPlayerApproximatorConfig(new ApproximatorConfigBuilder()
                .setApproximatorType(ApproximatorType.HASHMAP_LR)
                .setDataAggregationAlgorithm(DataAggregationAlgorithm.FIRST_VISIT_MC)
                .setLearningRate(0.01)
                .build())

            .setOpponentApproximatorConfig(new ApproximatorConfigBuilder()
                .setApproximatorType(ApproximatorType.HASHMAP_LR)
                .setDataAggregationAlgorithm(DataAggregationAlgorithm.FIRST_VISIT_MC)
                .setLearningRate(0.01)
                .build())

            // REINFORCEMENT
            .discountFactor(1)
            .batchEpisodeCount(batchEpisodeSize)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(50))
            .stageCount(25)

            .evaluatorType(EvaluatorType.RALF)
//            .setBatchedEvaluationSize(0)

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
                    return Math.exp(-callCount / 10000.0);
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
            .setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY);
    }

}
