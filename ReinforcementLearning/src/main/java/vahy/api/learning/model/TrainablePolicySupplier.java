package vahy.api.learning.model;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.DoubleVectorialReward;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicySupplier;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.List;

public interface TrainablePolicySupplier<
    TAction extends Action,
    TReward extends DoubleVectorialReward,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    extends PolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> {

    Policy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> initializePolicyWithExploration(State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> initialState);

    void train(List<ImmutableTuple<TPlayerObservation, TReward>> episodeData);
}
