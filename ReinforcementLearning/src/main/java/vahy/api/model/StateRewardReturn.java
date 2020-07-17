package vahy.api.model;

import vahy.api.model.observation.Observation;

public interface StateRewardReturn<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>> {

    double[] getReward();

    TState getState();

}
