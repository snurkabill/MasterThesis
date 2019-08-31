package vahy.impl.model;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.utils.ImmutableTuple;

public class ImmutableStateRewardReturnTuple<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends ImmutableTuple<TState, Double>
    implements StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState> {

    public ImmutableStateRewardReturnTuple(TState state, Double reward) {
        super(state, reward);
    }

    @Override
    public double getReward() {
        return super.getSecond();
    }

    @Override
    public TState getState() {
        return super.getFirst();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
