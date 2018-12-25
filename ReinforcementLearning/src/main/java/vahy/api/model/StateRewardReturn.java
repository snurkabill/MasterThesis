package vahy.api.model;

import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;

public interface StateRewardReturn<
    TAction extends Action,
    TReward extends Reward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> {

    TReward getReward();

    TState getState();

}
