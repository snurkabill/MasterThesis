package vahy.api.model;

import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;

public interface StateActionReward<
    TAction extends Action,
    TReward extends Reward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> {

    TAction getAction();

    TReward getReward();

    TState getState();

}
