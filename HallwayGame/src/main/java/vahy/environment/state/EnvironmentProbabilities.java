package vahy.environment.state;

import vahy.api.model.observation.FixedModelObservation;
import vahy.environment.HallwayAction;
import vahy.utils.ImmutableTuple;

import java.util.List;

public class EnvironmentProbabilities implements FixedModelObservation<HallwayAction> {

    private final ImmutableTuple<List<HallwayAction>, List<Double>> probabilities;

    public EnvironmentProbabilities(ImmutableTuple<List<HallwayAction>, List<Double>> probabilities) {
        this.probabilities = probabilities;
    }

    @Override
    public ImmutableTuple<List<HallwayAction>, List<Double>> getProbabilities() {
        return probabilities;
    }
}
