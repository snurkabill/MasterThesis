package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateActionReward;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.utils.ImmutableTuple;

import java.util.List;

public interface EpisodeResults<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    List<StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState>> getEpisodeStateRewardReturnList();

    List<ImmutableTuple<StateActionReward<TAction, TPlayerObservation, TOpponentObservation, TState>, StepRecord>> getEpisodeHistoryList();

    long getMillisecondDuration();

    String printActionHistory();

    TState getFinalState();

}
