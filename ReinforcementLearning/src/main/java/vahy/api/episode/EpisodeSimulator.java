package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;

public interface EpisodeSimulator<
    TAction extends Enum<TAction> & Action,
    TReward extends Reward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> {

    EpisodeResults<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> calculateEpisode();

}
