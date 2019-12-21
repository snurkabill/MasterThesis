package vahy.environment;

import vahy.impl.episode.AbstractInitialStateSupplier;
import vahy.impl.model.observation.DoubleVector;

import java.util.SplittableRandom;

public class RandomWalkInitialInstanceSupplier extends AbstractInitialStateSupplier<RandomWalkSetup, RandomWalkAction, DoubleVector, RandomWalkProbabilities, RandomWalkState> {

    public RandomWalkInitialInstanceSupplier(RandomWalkSetup randomWalkSetup, SplittableRandom random) {
        super(randomWalkSetup, random);
    }

    @Override
    protected RandomWalkState createState_inner(RandomWalkSetup problemConfig, SplittableRandom random) {
        return new RandomWalkState(problemConfig);
    }
}
