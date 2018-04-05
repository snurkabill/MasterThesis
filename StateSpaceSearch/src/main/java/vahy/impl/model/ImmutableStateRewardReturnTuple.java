package vahy.impl.model;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.utils.ImmutableTuple;

public class ImmutableStateRewardReturnTuple<TAction extends Action, TReward extends Reward, TObservation extends Observation, TState extends State<TAction, TReward, TObservation>>
    extends ImmutableTuple<TState, TReward>
    implements StateRewardReturn<TAction, TReward, TObservation, TState> {

    public ImmutableStateRewardReturnTuple(TState state, TReward reward) {
        super(state, reward);
    }

    @Override
    public TReward getReward() {
        return super.getSecond();
    }

    @Override
    public TState getState() {
        return super.getFirst();
    }
}
