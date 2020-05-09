package vahy.impl.runner;

import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.benchmark.EpisodeStatisticsCalculator;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.experiment.AlgorithmConfig;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.api.policy.PolicySupplier;
import vahy.impl.episode.DataPointGeneratorGeneric;
import vahy.impl.learning.trainer.PredictorTrainingSetup;

import java.util.List;

public class RunnerArguments<TConfig extends ProblemConfig,
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>,
    TPolicyRecord extends PolicyRecord,
    TStatistics extends EpisodeStatistics> {

    private final String policyId;

    private final TConfig problemConfig;
    private final SystemConfig systemConfig;
    private final AlgorithmConfig algorithmConfig;

    private final InitialStateSupplier<TAction, TObservation, TState> initialStateSupplier;
    private final EpisodeResultsFactory<TAction, TObservation, TState, TPolicyRecord> episodeResultsFactory;

    private final EpisodeStatisticsCalculator<TAction, TObservation, TState, TPolicyRecord, TStatistics> episodeStatisticsCalculator;
    private final List<DataPointGeneratorGeneric<TStatistics>> additionalDataPointGeneratorList;

    private final List<PredictorTrainingSetup<TAction, TObservation, TState, TPolicyRecord>> trainablePredictorSetupList;

    private final List<PolicySupplier<TAction, TObservation, TState, TPolicyRecord>> policySupplierList;

    private final EpisodeWriter<TAction, TObservation, TState, TPolicyRecord> episodeWriter;

    public RunnerArguments(String policyId, TConfig problemConfig,
                           SystemConfig systemConfig,
                           AlgorithmConfig algorithmConfig,
                           InitialStateSupplier<TAction, TObservation, TState> initialStateSupplier,
                           EpisodeResultsFactory<TAction, TObservation, TState, TPolicyRecord> episodeResultsFactory,
                           EpisodeStatisticsCalculator<TAction, TObservation, TState, TPolicyRecord, TStatistics> episodeStatisticsCalculator,
                           List<DataPointGeneratorGeneric<TStatistics>> additionalDataPointGeneratorList,
                           List<PolicySupplier<TAction, TObservation, TState, TPolicyRecord>> policySupplier,
                           EpisodeWriter<TAction, TObservation, TState, TPolicyRecord> episodeWriter,
                           List<PredictorTrainingSetup<TAction, TObservation, TState, TPolicyRecord>> trainablePredictorSetupList) {
        this.policyId = policyId;
        this.problemConfig = problemConfig;
        this.systemConfig = systemConfig;
        this.algorithmConfig = algorithmConfig;
        this.initialStateSupplier = initialStateSupplier;
        this.episodeResultsFactory = episodeResultsFactory;
        this.episodeStatisticsCalculator = episodeStatisticsCalculator;
        this.additionalDataPointGeneratorList = additionalDataPointGeneratorList;
        this.policySupplierList = policySupplier;
        this.episodeWriter = episodeWriter;
        this.trainablePredictorSetupList = trainablePredictorSetupList;
    }

    public TConfig getProblemConfig() {
        return problemConfig;
    }

    public SystemConfig getSystemConfig() {
        return systemConfig;
    }

    public AlgorithmConfig getAlgorithmConfig() {
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

    public List<PolicySupplier<TAction, TObservation, TState, TPolicyRecord>> getPolicySupplierList() {
        return policySupplierList;
    }

    public EpisodeWriter<TAction, TObservation, TState, TPolicyRecord> getEpisodeWriter() {
        return episodeWriter;
    }

    public List<PredictorTrainingSetup<TAction, TObservation, TState, TPolicyRecord>> getTrainablePredictorSetupList() {
        return trainablePredictorSetupList;
    }

    public String getPolicyId() {
        return policyId;
    }
}
