package vahy.impl.model;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.utils.ImmutableTuple;

public class ImmutableStateRewardReturn<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>>
    extends ImmutableTuple<TState, double[]>
    implements StateRewardReturn<TAction, TObservation, TState> {

    public ImmutableStateRewardReturn(TState state, double[] reward) {
        super(state, reward);
    }

    @Override
    public double[] getReward() {
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
