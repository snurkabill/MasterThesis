package vahy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.TrainerAlgorithm;
import vahy.environment.RandomWalkSetup;
import vahy.experiment.Experiment;
import vahy.experiment.ExperimentSetup;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.paperGenerics.reinforcement.learning.ApproximatorType;
import vahy.utils.ImmutableTuple;
import vahy.utils.ThirdPartBinaryUtils;

import java.io.IOException;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class RandomWalkExample {

    private static Logger logger = LoggerFactory.getLogger(RandomWalkExample.class.getName());

    public static void main(String[] args) throws IOException {
        ThirdPartBinaryUtils.cleanUpNativeTempFiles();

        //  EXAMPLE 1
        ImmutableTuple<RandomWalkSetup, ExperimentSetup> setup = createExperiment1();
        SplittableRandom random = new SplittableRandom(setup.getSecond().getRandomSeed());
        new Experiment().prepareAndRun(setup, random);
    }

    public static ImmutableTuple<RandomWalkSetup, ExperimentSetup> createExperiment1() {
        var startLevel = 5;
        var diffLevel = 20;
        var finishlevel = startLevel + diffLevel;
        var stepPenalty = 1;
        var randomWalkSetup = new RandomWalkSetup(finishlevel, startLevel, stepPenalty, 2, 2, 5, 9, 0.9, 0.8);

        var riskAllowed = 0.05;
        var batchSize = 100;

        ExperimentSetup experimentSetup = new ExperimentSetup(
            0,
            2,
            1,
            new FixedUpdateCountTreeConditionFactory(50),
            1.0,
            batchSize,
            20000,
            10000,
            100,
            new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    var x = Math.exp(-callCount / 100000.0) / 5;
//                    return 0.2;
                    if(callCount % batchSize == 0) {
                        logger.info("Exploration constant: [{}] in call: [{}]", x, callCount);
                    }
                    return x;
                }
            },
            new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    var x = Math.exp(-callCount / 100000.0) * 4;
                    if(callCount % batchSize == 0) {
                        logger.info("Temperature constant: [{}] in call: [{}]", x, callCount);
                    }
                    return x;
                }
            },
            () -> riskAllowed,
//            () -> 0.1,
//            () -> 2.0,
            TrainerAlgorithm.EVERY_VISIT_MC,
//            ApproximatorType.NN,
            ApproximatorType.HASHMAP_LR,
            EvaluatorType.RALF,
            1,
            1,
            1000,
            riskAllowed,
            0.01,
            InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW,
            InferenceNonExistingFlowStrategy.MAX_UCB_VISIT,
            ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE,
            ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VISIT,
            FlowOptimizerType.HARD_HARD,
            SubTreeRiskCalculatorType.FLOW_SUM,
            SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY,
            false);
        return new ImmutableTuple<>(randomWalkSetup, experimentSetup);
    }


//    public static ImmutableTuple<RandomWalkSetup, ExperimentSetup> createExperiment1() {
//        var randomWalkSetup = new RandomWalkSetup(100, 50, 1, 1, 10, 10, 0.9, 1.0);
//        ExperimentSetup experimentSetup = new ExperimentSetup(
//            0,
//            3,
//            1,
//            new FixedUpdateCountTreeConditionFactory(100),
//            1.0,
//            100,
//            20000,
//            10000,
//            100,
//            new Supplier<>() {
//                private int callCount = 0;
//                @Override
//                public Double get() {
//                    callCount++;
//                    return Math.exp(-callCount / 10000.0) / 2;
////                    return 0.2;
//                }
//            },
//            new Supplier<>() {
//                private int callCount = 0;
//                @Override
//                public Double get() {
//                    callCount++;
//                    return Math.exp(-callCount / 10000.0) * 4;
//                }
//            },
////            () -> 0.1,
////            () -> 2.0,
//            TrainerAlgorithm.EVERY_VISIT_MC,
////            ApproximatorType.NN,
//            ApproximatorType.HASHMAP_LR,
//            4,
//            100,
//            1000,
//            0.1,
//            0.1,
//            InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW,
//            InferenceNonExistingFlowStrategy.MAX_UCB_VISIT,
//            ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE,
//            ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VISIT,
//            FlowOptimizerType.HARD_HARD_SOFT,
//            SubTreeRiskCalculatorType.FLOW_SUM,
//            SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY,
//            false);
//        return new ImmutableTuple<>(randomWalkSetup, experimentSetup);
//    }

}

