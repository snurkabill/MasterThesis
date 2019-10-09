package vahy.impl.episode;

import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeStepRecord;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;

import java.time.Duration;
import java.util.List;

public class EpisodeResultsImpl<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord>
    implements EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> {

    private final List<EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> episodeHistory;
    private final int playerStepCount;
    private final int totalStepCount;
    private final double totalPayoff;
    private final Duration duration;

    public EpisodeResultsImpl(List<EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> episodeHistory,
                              int playerStepCount,
                              int totalStepCount,
                              double totalPayoff,
                              Duration duration) {
        this.episodeHistory = episodeHistory;
        this.playerStepCount = playerStepCount;
        this.totalStepCount = totalStepCount;
        this.totalPayoff = totalPayoff;
        this.duration = duration;
    }


    @Override
    public List<EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> getEpisodeHistory() {
        return episodeHistory;
    }

    @Override
    public int getTotalStepCount() {
        return totalStepCount;
    }

    @Override
    public int getPlayerStepCount() {
        return playerStepCount;
    }

    @Override
    public double getTotalPayoff() {
        return totalPayoff;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public TState getFinalState() {
        return episodeHistory.get(episodeHistory.size() - 1).getToState();
    }
}
