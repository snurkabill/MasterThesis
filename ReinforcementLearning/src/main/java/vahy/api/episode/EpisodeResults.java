package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateActionReward;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.utils.ImmutableTuple;

import java.util.List;

public interface EpisodeResults<
    TAction extends Enum<TAction> & Action,
    TReward extends Reward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> {

    List<StateRewardReturn<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> getEpisodeStateRewardReturnList();

    List<ImmutableTuple<StateActionReward<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>, StepRecord<TReward>>> getEpisodeHistoryList();

    long getMillisecondDuration();

    String printActionHistory();

    TState getFinalState();

}
