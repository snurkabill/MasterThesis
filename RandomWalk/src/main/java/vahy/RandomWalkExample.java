package vahy;

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

    public static void main(String[] args) throws IOException {
        ThirdPartBinaryUtils.cleanUpNativeTempFiles();

        //  EXAMPLE 1
        ImmutableTuple<RandomWalkSetup, ExperimentSetup> setup = createExperiment1();
        SplittableRandom random = new SplittableRandom(setup.getSecond().getRandomSeed());
        new Experiment().prepareAndRun(setup, random);
    }

    public static ImmutableTuple<RandomWalkSetup, ExperimentSetup> createExperiment1() {
        var randomWalkSetup = new RandomWalkSetup(50, 50, 1, 1, 10, 10, 0.9, 0.7);
        ExperimentSetup experimentSetup = new ExperimentSetup(
            0,
            2,
            1,
            new FixedUpdateCountTreeConditionFactory(10),
            1.0,
            100,
            20000,
            10000,
            30,
            new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                     return Math.exp(-callCount / 10000.0) / 5;
//                    return 0.2;
                }
            },
            new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    return Math.exp(-callCount / 10000.0) * 4;
                }
            },
            () -> 0.0,
//            () -> 0.1,
//            () -> 2.0,
            TrainerAlgorithm.EVERY_VISIT_MC,
//            ApproximatorType.NN,
            ApproximatorType.HASHMAP_LR,
            128,
            100,
            1000,
            0.0,
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

