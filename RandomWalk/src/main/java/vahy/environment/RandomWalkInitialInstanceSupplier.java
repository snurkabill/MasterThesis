package vahy.environment;

import vahy.api.episode.InitialStateSupplier;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;

public class RandomWalkInitialInstanceSupplier implements InitialStateSupplier<RandomWalkAction, DoubleReward, DoubleVector, RandomWalkProbabilities, RandomWalkState> {

    private final RandomWalkSetup randomWalkSetup;

    public RandomWalkInitialInstanceSupplier(RandomWalkSetup randomWalkSetup) {
        this.randomWalkSetup = randomWalkSetup;
    }

    @Override
    public RandomWalkState createInitialState() {
        return new RandomWalkState(randomWalkSetup);
    }
}
