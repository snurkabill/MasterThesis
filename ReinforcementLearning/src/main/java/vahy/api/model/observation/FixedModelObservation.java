package vahy.api.model.observation;

import vahy.api.model.Action;
import vahy.utils.ImmutableTuple;

import java.util.List;

public interface FixedModelObservation<TAction extends Enum<TAction> & Action> extends Observation {

    ImmutableTuple<List<TAction>, List<Double>> getProbabilities();

}
