package vahy.impl.benchmark;

import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.episode.EpisodeResults;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;

import java.time.Duration;
import java.util.List;

public class PolicyResults<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord,
    TStatistics extends EpisodeStatistics> {

    private final BenchmarkedPolicy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policy;
    private final List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> episodeList;
    private final TStatistics episodeStatistics;
    private final Duration benchmarkingDuration;

    public PolicyResults(BenchmarkedPolicy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policy,
                         List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> episodeList,
                         TStatistics episodeStatistics,
                         Duration benchmarkingDuration) {
        this.policy = policy;
        this.episodeList = episodeList;
        this.episodeStatistics = episodeStatistics;
        this.benchmarkingDuration = benchmarkingDuration;
    }

    public BenchmarkedPolicy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getPolicy() {
        return policy;
    }

    public List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> getEpisodeList() {
        return episodeList;
    }

    public Duration getBenchmarkingDuration() {
        return benchmarkingDuration;
    }

    public TStatistics getEpisodeStatistics() {
        return episodeStatistics;
    }

}