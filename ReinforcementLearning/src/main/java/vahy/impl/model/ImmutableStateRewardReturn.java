package vahy.impl.model;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.utils.ImmutableTriple;

public class ImmutableStateRewardReturn<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>>
    extends ImmutableTriple<TState, double[], TAction[]>
    implements StateRewardReturn<TAction, TObservation, TState> {

    public ImmutableStateRewardReturn(TState state, double[] reward, TAction[] action) {
        super(state, reward, action);
    }

    @Override
    public double[] getReward() {
        return super.getSecond();
    }

    @Override
    public TAction[] getAction() {
        return super.getThird();
    }

    @Override
    public TState getState() {
        return super.getFirst();
    }

}
