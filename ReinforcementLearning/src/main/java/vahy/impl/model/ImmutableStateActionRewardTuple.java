package vahy.impl.model;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateActionReward;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.utils.ImmutableTuple;

public class ImmutableStateActionRewardTuple<
    TAction extends Action,
    TReward extends Reward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    extends ImmutableTuple<ImmutableTuple<TAction, TReward>, TState>
    implements StateActionReward<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> {

    public ImmutableStateActionRewardTuple(TState state, TAction action, TReward reward) {
        super(new ImmutableTuple<>(action, reward), state);
    }

    @Override
    public TAction getAction() {
        return super.getFirst().getFirst();
    }

    @Override
    public TReward getReward() {
        return super.getFirst().getSecond();
    }

    @Override
    public TState getState() {
        return super.getSecond();
    }
}
