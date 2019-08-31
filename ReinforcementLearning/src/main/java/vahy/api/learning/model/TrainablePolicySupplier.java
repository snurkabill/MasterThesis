package vahy.api.learning.model;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicySupplier;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.List;

public interface TrainablePolicySupplier<
    TAction extends Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState> {

    Policy<TAction, TPlayerObservation, TOpponentObservation, TState> initializePolicyWithExploration(State<TAction, TPlayerObservation, TOpponentObservation, TState> initialState);

    void train(List<ImmutableTuple<TPlayerObservation, Double>> episodeData);
}
