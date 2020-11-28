package vahy.impl.model;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.utils.ImmutableQuadriple;

public class ImmutableStateWrapperRewardReturn<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TState extends State<TAction, TObservation, TState>>
    extends ImmutableQuadriple<StateWrapper<TAction, TObservation, TState>, Double, double[], TAction>
    implements StateWrapperRewardReturn<TAction, TObservation, TState> {

    public ImmutableStateWrapperRewardReturn(StateWrapper<TAction, TObservation, TState> first) {
        this(first, 0.0, null, null);
    }

    public ImmutableStateWrapperRewardReturn(StateWrapper<TAction, TObservation, TState> first, Double second, double[] third, TAction fourth) {
        super(first, second, third, fourth);
    }

    @Override
    public Double getReward() {
        return super.getSecond();
    }

    @Override
    public double[] getAllPlayerRewards() {
        return super.getThird();
    }

    @Override
    public TAction getObservedActionPlayed() {
        return super.getFourth();
    }

    @Override
    public StateWrapper<TAction, TObservation, TState> getState() {
        return super.getFirst();
    }

}
