package vahy.impl.model;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.utils.ImmutableTriple;

public class ImmutableStateWrapperRewardReturn<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>>
    extends ImmutableTriple<StateWrapper<TAction, TObservation, TState>, Double, double[]>
    implements StateWrapperRewardReturn<TAction, TObservation, TState> {

    public ImmutableStateWrapperRewardReturn(StateWrapper<TAction, TObservation, TState> first, Double second, double[] third) {
        super(first, second, third);
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
    public StateWrapper<TAction, TObservation, TState> getState() {
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
