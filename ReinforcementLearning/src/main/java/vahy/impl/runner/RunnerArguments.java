package vahy.impl.runner;

import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.benchmark.EpisodeStatisticsCalculator;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.impl.episode.DataPointGeneratorGeneric;

import java.util.List;

public class RunnerArguments<TConfig extends ProblemConfig,
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>,
    TPolicyRecord extends PolicyRecord,
    TStatistics extends EpisodeStatistics> {

    private final String runName;

    private final TConfig problemConfig;
    private final SystemConfig systemConfig;
    private final CommonAlgorithmConfig algorithmConfig;

    private final InitialStateSupplier<TAction, TObservation, TState> initialStateSupplier;
    private final EpisodeStatisticsCalculator<TAction, TObservation, TState, TPolicyRecord, TStatistics> episodeStatisticsCalculator;
    private final List<DataPointGeneratorGeneric<TStatistics>> additionalDataPointGeneratorList;
    private final EpisodeResultsFactory<TAction, TObservation, TState, TPolicyRecord> episodeResultsFactory;

    private final List<PolicyArguments<TAction, TObservation, TState, TPolicyRecord>> policyArgumentsList;

    private final EpisodeWriter<TAction, TObservation, TState, TPolicyRecord> episodeWriter;

    public RunnerArguments(String runName,
                           TConfig problemConfig,
                           SystemConfig systemConfig,
                           CommonAlgorithmConfig algorithmConfig,
                           InitialStateSupplier<TAction, TObservation, TState> initialStateSupplier,
                           EpisodeResultsFactory<TAction, TObservation, TState, TPolicyRecord> episodeResultsFactory,
                           EpisodeStatisticsCalculator<TAction, TObservation, TState, TPolicyRecord, TStatistics> episodeStatisticsCalculator,
                           List<DataPointGeneratorGeneric<TStatistics>> additionalDataPointGeneratorList,
                           EpisodeWriter<TAction, TObservation, TState, TPolicyRecord> episodeWriter,
                           List<PolicyArguments<TAction, TObservation, TState, TPolicyRecord>> policyArgumentsList) {
        this.runName = runName;
        this.problemConfig = problemConfig;
        this.systemConfig = systemConfig;
        this.algorithmConfig = algorithmConfig;
        this.initialStateSupplier = initialStateSupplier;
        this.episodeResultsFactory = episodeResultsFactory;
        this.episodeStatisticsCalculator = episodeStatisticsCalculator;
        this.additionalDataPointGeneratorList = additionalDataPointGeneratorList;
        this.episodeWriter = episodeWriter;
        this.policyArgumentsList = policyArgumentsList;
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

    public CommonAlgorithmConfig getAlgorithmConfig() {
        return algorithmConfig;
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

    public List<DataPointGeneratorGeneric<TStatistics>> getAdditionalDataPointGeneratorList() {
        return additionalDataPointGeneratorList;
    }

    public EpisodeWriter<TAction, TObservation, TState, TPolicyRecord> getEpisodeWriter() {
        return episodeWriter;
    }

    public List<PolicyArguments<TAction, TObservation, TState, TPolicyRecord>> getPolicyArgumentsList() {
        return policyArgumentsList;
    }
}
