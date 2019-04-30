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
        var randomWalkSetup = new RandomWalkSetup(100, 50, 1, 1, 10, 10, 0.9, 0.7);
        ExperimentSetup experimentSetup = new ExperimentSetup(
            0,
            3,
            1,
            new FixedUpdateCountTreeConditionFactory(200),
            1.0,
            100,
            20000,
            10000,
            100,
            new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                     return Math.exp(-callCount / 2000.0) / 3;
//                    return 0.2;
                }
            },
            new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    return Math.exp(-callCount / 2000.0) * 3;
                }
            },
//            () -> 0.1,
//            () -> 2.0,
            TrainerAlgorithm.EVERY_VISIT_MC,
            ApproximatorType.NN,
//            ApproximatorType.HASHMAP,
            4,
            100,
            10000,
            0.0,
            0.01,
            InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW,
            InferenceNonExistingFlowStrategy.MAX_UCB_VALUE,
            ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE,
            ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VALUE,
            FlowOptimizerType.SOFT,
            SubTreeRiskCalculatorType.PRIOR_SUM,
            SubTreeRiskCalculatorType.PRIOR_SUM,
            false);
        return new ImmutableTuple<>(randomWalkSetup, experimentSetup);
    }

}
