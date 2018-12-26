package vahy.environment.state;

import vahy.api.model.observation.Observation;
import vahy.environment.HallwayAction;
import vahy.utils.ImmutableTuple;

import java.util.List;

public class EnvironmentProbabilities implements Observation {

    private final ImmutableTuple<List<HallwayAction>, List<Double>> probabilities;

    public EnvironmentProbabilities(ImmutableTuple<List<HallwayAction>, List<Double>> probabilities) {
        this.probabilities = probabilities;
    }

    public ImmutableTuple<List<HallwayAction>, List<Double>> getProbabilities() {
        return probabilities;
    }
}
