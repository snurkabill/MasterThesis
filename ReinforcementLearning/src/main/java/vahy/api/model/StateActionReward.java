package vahy.api.model;

import vahy.api.model.observation.Observation;

public interface StateActionReward<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    TAction getAction();

    double getReward();

    TState getState();

}
