package vahy.api.model;

import vahy.api.model.observation.Observation;

public interface StateWrapperRewardReturn<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TState extends State<TAction, TObservation, TState>> {

    Double getReward();

    double[] getAllPlayerRewards();

    TAction getObservedActionPlayed();

    StateWrapper<TAction, TObservation, TState> getState();

}
