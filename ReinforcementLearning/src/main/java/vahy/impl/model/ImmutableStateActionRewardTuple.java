package vahy.impl.model;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateActionReward;
import vahy.api.model.observation.Observation;
import vahy.utils.ImmutableTuple;

public class ImmutableStateActionRewardTuple<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends ImmutableTuple<ImmutableTuple<TAction, Double>, TState>
    implements StateActionReward<TAction, TPlayerObservation, TOpponentObservation, TState> {

    public ImmutableStateActionRewardTuple(TState state, TAction action, Double reward) {
        super(new ImmutableTuple<>(action, reward), state);
    }

    @Override
    public TAction getAction() {
        return super.getFirst().getFirst();
    }

    @Override
    public double  getReward() {
        return super.getFirst().getSecond();
    }

    @Override
    public TState getState() {
        return super.getSecond();
    }
}
