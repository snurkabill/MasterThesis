package vahy.impl.benchmark;

import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.episode.EpisodeResults;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.impl.model.observation.DoubleVector;

import java.util.List;

public class PolicyResults<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    private final BenchmarkedPolicy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policy;
    private final List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> episodeList;
    private final EpisodeStatistics episodeStatistics;
    private final long benchmarkingMilliseconds;

    public PolicyResults(BenchmarkedPolicy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policy,
                         List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> episodeList,
                         EpisodeStatistics episodeStatistics,
                         long benchmarkingMilliseconds) {
        this.policy = policy;
        this.episodeList = episodeList;
        this.episodeStatistics = episodeStatistics;
        this.benchmarkingMilliseconds = benchmarkingMilliseconds;
    }

    public BenchmarkedPolicy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getPolicy() {
        return policy;
    }

    public List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> getEpisodeList() {
        return episodeList;
    }

    public long getBenchmarkingMilliseconds() {
        return benchmarkingMilliseconds;
    }

    public EpisodeStatistics getEpisodeStatistics() {
        return episodeStatistics;
    }

}