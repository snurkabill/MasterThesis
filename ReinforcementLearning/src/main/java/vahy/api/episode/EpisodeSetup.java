package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.policy.Policy;

public interface EpisodeSetup<
    TAction extends Enum<TAction> & Action,
    TReward extends Reward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> {

    int getStepCountLimit();

    TState getInitialState();

    Policy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> getPlayerPaperPolicy();

    Policy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> getOpponentPolicy();

}
