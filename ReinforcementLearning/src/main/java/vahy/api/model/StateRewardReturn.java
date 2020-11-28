package vahy.api.model;

import vahy.api.model.observation.Observation;

public interface StateRewardReturn<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TState extends State<TAction, TObservation, TState>> {

    double[] getReward();

    TAction[] getAction();

    TState getState();

}
