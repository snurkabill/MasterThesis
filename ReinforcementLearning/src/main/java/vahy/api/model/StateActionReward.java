package vahy.api.model;

import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;

public interface StateActionReward<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TState extends State<TAction, TReward, TObservation>> {

    TAction getAction();

    TReward getReward();

    TState getState();

}
