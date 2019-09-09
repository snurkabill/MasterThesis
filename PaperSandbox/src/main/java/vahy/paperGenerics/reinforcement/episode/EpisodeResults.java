package vahy.paperGenerics.reinforcement.episode;

import vahy.api.model.Action;
import vahy.api.model.StateActionReward;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;
import vahy.utils.ImmutableTuple;

import java.util.List;
import java.util.stream.Collectors;

public class EpisodeResults<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>  {

    private final List<StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState>> episodeStateRewardReturnList;
    private final List<ImmutableTuple<StateActionReward<TAction, TPlayerObservation, TOpponentObservation, TState>, PolicyStepRecord>> episodeHistoryList;
    private final long millisecondDuration;

    public EpisodeResults(List<StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState>> episodeStateRewardReturnList,
                          List<ImmutableTuple<StateActionReward<TAction, TPlayerObservation, TOpponentObservation, TState>, PolicyStepRecord>> episodeHistoryList,
                          long millisecondDuration) {
        this.episodeStateRewardReturnList = episodeStateRewardReturnList;
        this.episodeHistoryList = episodeHistoryList;
        this.millisecondDuration = millisecondDuration;
    }

    public List<StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState>> getEpisodeStateRewardReturnList() {
        return episodeStateRewardReturnList;
    }

    public List<ImmutableTuple<StateActionReward<TAction, TPlayerObservation, TOpponentObservation, TState>, PolicyStepRecord>> getEpisodeHistoryList() {
        return episodeHistoryList;
    }

    public long getMillisecondDuration() {
        return millisecondDuration;
    }

    public String printActionHistory() {
        return "[" + episodeHistoryList.stream().map(x -> x.getFirst().getAction()).collect(Collectors.toList()) + "]";
    }

    public TState getFinalState() {
        return this.episodeStateRewardReturnList.get(episodeStateRewardReturnList.size() - 1).getState();
    }

    public boolean isRiskHit() {
        return this.getFinalState().isRiskHit();
    }

}
