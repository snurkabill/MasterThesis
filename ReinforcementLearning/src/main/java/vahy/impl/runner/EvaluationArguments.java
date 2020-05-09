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
import vahy.api.policy.PolicySupplier;

public class EvaluationArguments<TConfig extends ProblemConfig,
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>,
    TPolicyRecord extends PolicyRecord,
    TStatistics extends EpisodeStatistics> {

    private final TConfig problemConfig;
    private final SystemConfig systemConfig;

    private final InitialStateSupplier<TAction, TObservation, TState> initialStateSupplier;
    private final EpisodeResultsFactory<TAction, TObservation, TState, TPolicyRecord> episodeResultsFactory;
    private final EpisodeStatisticsCalculator<TAction, TObservation, TState, TPolicyRecord, TStatistics> episodeStatisticsCalculator;
    private final PolicySupplier<TAction, TObservation, TState, TPolicyRecord> opponentPolicySupplier;
    private final EpisodeWriter<TAction, TObservation, TState, TPolicyRecord> episodeWriter;

    public EvaluationArguments(TConfig problemConfig,
                               SystemConfig systemConfig,
                               InitialStateSupplier<TAction, TObservation, TState> initialStateSupplier,
                               EpisodeResultsFactory<TAction, TObservation, TState, TPolicyRecord> episodeResultsFactory,
                               EpisodeStatisticsCalculator<TAction, TObservation, TState, TPolicyRecord, TStatistics> episodeStatisticsCalculator,
                               PolicySupplier<TAction, TObservation, TState, TPolicyRecord> opponentPolicySupplier,
                               EpisodeWriter<TAction, TObservation, TState, TPolicyRecord> episodeWriter)
    {
        this.problemConfig = problemConfig;
        this.systemConfig = systemConfig;
        this.initialStateSupplier = initialStateSupplier;
        this.episodeResultsFactory = episodeResultsFactory;
        this.episodeStatisticsCalculator = episodeStatisticsCalculator;
        this.opponentPolicySupplier = opponentPolicySupplier;
        this.episodeWriter = episodeWriter;
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

    public PolicySupplier<TAction, TObservation, TState, TPolicyRecord> getOpponentPolicySupplier() {
        return opponentPolicySupplier;
    }

    public EpisodeWriter<TAction, TObservation, TState, TPolicyRecord> getEpisodeWriter() {
        return episodeWriter;
    }
}
