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
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord,
    TStatistics extends EpisodeStatistics> {

    private final String policyId;

    private final TConfig problemConfig;
    private final SystemConfig systemConfig;
    private final AlgorithmConfig algorithmConfig;

    private final InitialStateSupplier<TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier;
    private final EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeResultsFactory;

    private final EpisodeStatisticsCalculator<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord, TStatistics> episodeStatisticsCalculator;
    private final List<DataPointGeneratorGeneric<TStatistics>> additionalDataPointGeneratorList;

    private final List<PredictorTrainingSetup<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> trainablePredictorSetupList;

    private final PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> opponentPolicySupplier;

    private final PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policySupplier;
    private final EpisodeWriter<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeWriter;

    public RunnerArguments(String policyId, TConfig problemConfig,
                           SystemConfig systemConfig,
                           AlgorithmConfig algorithmConfig,
                           InitialStateSupplier<TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                           EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeResultsFactory,
                           EpisodeStatisticsCalculator<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord, TStatistics> episodeStatisticsCalculator,
                           List<DataPointGeneratorGeneric<TStatistics>> additionalDataPointGeneratorList,
                           PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> opponentPolicySupplier,
                           PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policySupplier,
                           EpisodeWriter<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeWriter,
                           List<PredictorTrainingSetup<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> trainablePredictorSetupList) {
        this.policyId = policyId;
        this.problemConfig = problemConfig;
        this.systemConfig = systemConfig;
        this.algorithmConfig = algorithmConfig;
        this.initialStateSupplier = initialStateSupplier;
        this.episodeResultsFactory = episodeResultsFactory;
        this.episodeStatisticsCalculator = episodeStatisticsCalculator;
        this.additionalDataPointGeneratorList = additionalDataPointGeneratorList;
        this.opponentPolicySupplier = opponentPolicySupplier;
        this.policySupplier = policySupplier;
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

    public InitialStateSupplier<TAction, TPlayerObservation, TOpponentObservation, TState> getInitialStateSupplier() {
        return initialStateSupplier;
    }

    public EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getEpisodeResultsFactory() {
        return episodeResultsFactory;
    }

    public EpisodeStatisticsCalculator<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord, TStatistics> getEpisodeStatisticsCalculator() {
        return episodeStatisticsCalculator;
    }

    public List<DataPointGeneratorGeneric<TStatistics>> getAdditionalDataPointGeneratorList() {
        return additionalDataPointGeneratorList;
    }

    public PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getOpponentPolicySupplier() {
        return opponentPolicySupplier;
    }

    public PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getPolicySupplier() {
        return policySupplier;
    }

    public EpisodeWriter<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getEpisodeWriter() {
        return episodeWriter;
    }

    public List<PredictorTrainingSetup<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> getTrainablePredictorSetupList() {
        return trainablePredictorSetupList;
    }

    public String getPolicyId() {
        return policyId;
    }
}
