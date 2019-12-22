package vahy.resignation.environment.state;

import vahy.api.model.observation.FixedModelObservation;
import vahy.resignation.environment.HallwayActionWithResign;
import vahy.utils.ImmutableTuple;

import java.util.List;

public class EnvironmentProbabilities implements FixedModelObservation<HallwayActionWithResign> {

    private final ImmutableTuple<List<HallwayActionWithResign>, List<Double>> probabilities;

    public EnvironmentProbabilities(ImmutableTuple<List<HallwayActionWithResign>, List<Double>> probabilities) {
        this.probabilities = probabilities;
    }

    @Override
    public ImmutableTuple<List<HallwayActionWithResign>, List<Double>> getProbabilities() {
        return probabilities;
    }
}
