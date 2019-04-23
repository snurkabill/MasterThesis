package vahy;

import vahy.api.episode.TrainerAlgorithm;
import vahy.environment.RandomWalkSetup;
import vahy.experiment.Experiment;
import vahy.experiment.ExperimentSetup;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
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
        var randomWalkSetup = new RandomWalkSetup(100, 15, 1, 1, 10, 10, 0.9, 0.8);
        ExperimentSetup experimentSetup = new ExperimentSetup(
            0,
            3,
            1,
            new FixedUpdateCountTreeConditionFactory(200),
            1.0,
            10,
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
            0.3,
            0.01,
            false);
        return new ImmutableTuple<>(randomWalkSetup, experimentSetup);
    }

}
