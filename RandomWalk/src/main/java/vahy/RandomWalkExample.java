package vahy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.experiment.StochasticStrategy;
import vahy.api.experiment.SystemConfig;
import vahy.api.experiment.SystemConfigBuilder;
import vahy.api.learning.ApproximatorType;
import vahy.api.learning.dataAggregator.DataAggregationAlgorithm;
import vahy.config.AlgorithmConfigBuilder;
import vahy.config.EvaluatorType;
import vahy.config.PaperAlgorithmConfig;
import vahy.config.SelectorType;
import vahy.environment.RandomWalkAction;
import vahy.environment.RandomWalkInitialInstanceSupplier;
import vahy.environment.RandomWalkSetup;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.opponent.RandomWalkOpponentSupplier;
import vahy.paperGenerics.PaperExperimentEntryPoint;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.utils.ThirdPartBinaryUtils;

import java.nio.file.Path;
import java.util.function.Supplier;

public class RandomWalkExample {

    public static final Logger logger = LoggerFactory.getLogger(RandomWalkExample.class.getName());

    public static void main(String[] args) {
        ThirdPartBinaryUtils.cleanUpNativeTempFiles();

        var algorithmConfig = createAlgorithmConfig();
        var systemConfig = createSystemConfig();
        var problemConfig = createGameConfig();

        PaperExperimentEntryPoint.createExperimentAndRun(
            RandomWalkAction.class,
            RandomWalkInitialInstanceSupplier::new,
            RandomWalkOpponentSupplier.class,
            algorithmConfig,
            systemConfig,
            problemConfig,
            Path.of("Results")
        );
    }


    public static PaperAlgorithmConfig createAlgorithmConfig() {

        var riskAllowed = 1.0;
        var batchSize = 1000;

        return new AlgorithmConfigBuilder()
            //MCTS
            .cpuctParameter(1)

            //.mcRolloutCount(1)
            //NN
            .trainingBatchSize(1)
            .trainingEpochCount(10)
            // REINFORCEMENT
            .discountFactor(1)
            .batchEpisodeCount(batchSize)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(50))
            .stageCount(100)
            .evaluatorType(EvaluatorType.RALF)
//            .setBatchedEvaluationSize(1)
            .maximalStepCountBound(500)
            .trainerAlgorithm(DataAggregationAlgorithm.EVERY_VISIT_MC)
            .replayBufferSize(100_000)
            .learningRate(0.01)

            .approximatorType(ApproximatorType.HASHMAP_LR)
            .globalRiskAllowed(riskAllowed)
            .riskSupplier(new Supplier<Double>() {
                @Override
                public Double get() {
                    return riskAllowed;
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
                    return 2.00;
                }

                @Override
                public String toString() {
                    return "() -> 1.05";
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

    public static SystemConfig createSystemConfig() {
        return new SystemConfigBuilder()
            .setRandomSeed(0)
            .setStochasticStrategy(StochasticStrategy.REPRODUCIBLE)
            .setDrawWindow(true)
            .setParallelThreadsCount(4)
            .setSingleThreadedEvaluation(false)
            .setEvalEpisodeCount(1000)
            .buildSystemConfig();
    }

    public static RandomWalkSetup createGameConfig() {
        var startLevel = 5;
        var diffLevel = 100;
        var finishlevel = startLevel + diffLevel;
        var stepPenalty = 1;
        return new RandomWalkSetup(
            finishlevel,
            startLevel,
            stepPenalty,
            2,
            2,
            5,
            9,
            0.9,
            0.8);
    }

//    public static ImmutableTuple<RandomWalkSetup, ExperimentSetup> createExperiment() {
//
//        var riskAllowed = 1.00;
//        var batchSize = 1000;
//
//        ExperimentSetup experimentSetup = new ExperimentSetup(
//            0,
//            2,
//            1,
//            new FixedUpdateCountTreeConditionFactory(1000),
//            1.0,
//            batchSize,
//            20000,
//            10000,
//            1000,
//            new Supplier<>() {
//                private int callCount = 0;
//                @Override
//                public Double get() {
//                    callCount++;
//                    var x = Math.exp(-callCount / 100000.0) / 5;
////                    return 0.2;
//                    if(callCount % batchSize == 0) {
//                        logger.info("Exploration constant: [{}] in call: [{}]", x, callCount);
//                    }
//                    return x;
//                }
//            },
//            new Supplier<>() {
//                private int callCount = 0;
//                @Override
//                public Double get() {
//                    callCount++;
//                    var x = Math.exp(-callCount / 100000.0) * 4;
//                    if(callCount % batchSize == 0) {
//                        logger.info("Temperature constant: [{}] in call: [{}]", x, callCount);
//                    }
//                    return x;
//                }
//            },
//            () -> riskAllowed,
////            () -> 0.1,
////            () -> 2.0,
//            DataAggregationAlgorithm.EVERY_VISIT_MC,
////            ApproximatorType.NN,
//            ApproximatorType.HASHMAP_LR,
//            EvaluatorType.RALF,
//            1,
//            1,
//            1000,
//            riskAllowed,
//            0.01,
//            InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW,
//            InferenceNonExistingFlowStrategy.MAX_UCB_VISIT,
//            ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE,
//            ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VISIT,
//            FlowOptimizerType.HARD_HARD,
//            SubTreeRiskCalculatorType.FLOW_SUM,
//            SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY,
//            false);
//        return new ImmutableTuple<>(randomWalkSetup, experimentSetup);
//    }
}

