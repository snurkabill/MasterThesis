package vahy.impl.benchmark;

import vahy.api.benchmark.EpisodeStatistics;
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

    private final OptimizedPolicy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policy;
    private final List<TStatistics> trainingStatisticsList;
    private final TStatistics episodeStatistics;
    private final Duration trainingDuration;
    private final Duration benchmarkingDuration;

    public PolicyResults(OptimizedPolicy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policy,
                         List<TStatistics> trainingStatisticsList,
                         TStatistics episodeStatistics,
                         Duration trainingDuration,
                         Duration benchmarkingDuration) {
        this.policy = policy;
        this.trainingStatisticsList = trainingStatisticsList;
        this.episodeStatistics = episodeStatistics;
        this.trainingDuration = trainingDuration;
        this.benchmarkingDuration = benchmarkingDuration;
    }

    public OptimizedPolicy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getPolicy() {
        return policy;
    }

    public List<TStatistics> getTrainingStatisticsList() {
        return trainingStatisticsList;
    }

    public Duration getTrainingDuration() {
        return trainingDuration;
    }

    public Duration getBenchmarkingDuration() {
        return benchmarkingDuration;
    }

    public TStatistics getEpisodeStatistics() {
        return episodeStatistics;
    }

}