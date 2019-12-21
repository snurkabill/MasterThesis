package vahy.environment;

import vahy.api.model.observation.FixedModelObservation;
import vahy.utils.ImmutableTuple;

import java.util.List;

public class RandomWalkProbabilities implements FixedModelObservation<RandomWalkAction> {

    private final ImmutableTuple<List<RandomWalkAction>, List<Double>> probabilities;

    public RandomWalkProbabilities(ImmutableTuple<List<RandomWalkAction>, List<Double>> probabilities) {
        this.probabilities = probabilities;
    }

    public ImmutableTuple<List<RandomWalkAction>, List<Double>> getProbabilities() {
        return probabilities;
    }
}
