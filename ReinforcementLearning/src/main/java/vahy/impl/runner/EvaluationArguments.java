package vahy.impl.runner;

import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.benchmark.EpisodeStatisticsCalculator;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;

public class EvaluationArguments<TConfig extends ProblemConfig,
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>,
    TPolicyRecord extends PolicyRecord,
    TStatistics extends EpisodeStatistics> {

    private final String runName;

    private final TConfig problemConfig;
    private final SystemConfig systemConfig;

    private final InitialStateSupplier<TAction, TObservation, TState> initialStateSupplier;
    private final EpisodeResultsFactory<TAction, TObservation, TState, TPolicyRecord> episodeResultsFactory;
    private final EpisodeStatisticsCalculator<TAction, TObservation, TState, TPolicyRecord, TStatistics> episodeStatisticsCalculator;
    private final EpisodeWriter<TAction, TObservation, TState, TPolicyRecord> episodeWriter;

    public EvaluationArguments(String runName, TConfig problemConfig,
                               SystemConfig systemConfig,
                               InitialStateSupplier<TAction, TObservation, TState> initialStateSupplier,
                               EpisodeResultsFactory<TAction, TObservation, TState, TPolicyRecord> episodeResultsFactory,
                               EpisodeStatisticsCalculator<TAction, TObservation, TState, TPolicyRecord, TStatistics> episodeStatisticsCalculator,
                               EpisodeWriter<TAction, TObservation, TState, TPolicyRecord> episodeWriter)
    {
        this.runName = runName;
        this.problemConfig = problemConfig;
        this.systemConfig = systemConfig;
        this.initialStateSupplier = initialStateSupplier;
        this.episodeResultsFactory = episodeResultsFactory;
        this.episodeStatisticsCalculator = episodeStatisticsCalculator;
        this.episodeWriter = episodeWriter;
    }

    public String getRunName() {
        return runName;
    }

    public TConfig getProblemConfig() {
        return problemConfig;
    }

    public SystemConfig getSystemConfig() {
        return systemConfig;
    }

    public InitialStateSupplier<TAction, TObservation, TState> getInitialStateSupplier() {
        return initialStateSupplier;
    }

    public EpisodeResultsFactory<TAction, TObservation, TState, TPolicyRecord> getEpisodeResultsFactory() {
        return episodeResultsFactory;
    }

    public EpisodeStatisticsCalculator<TAction, TObservation, TState, TPolicyRecord, TStatistics> getEpisodeStatisticsCalculator() {
        return episodeStatisticsCalculator;
    }

    public EpisodeWriter<TAction, TObservation, TState, TPolicyRecord> getEpisodeWriter() {
        return episodeWriter;
    }
}
