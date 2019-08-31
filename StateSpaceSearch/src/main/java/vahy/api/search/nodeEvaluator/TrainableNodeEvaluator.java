package vahy.api.search.nodeEvaluator;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.utils.ImmutableTuple;

import java.util.List;

public interface TrainableNodeEvaluator<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends NodeEvaluator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    void train(List<ImmutableTuple<TPlayerObservation, double[]>> trainData);

    double[] evaluate(TPlayerObservation observation);

}
