package vahy.impl.runner;

import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.benchmark.EpisodeStatisticsCalculator;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.experiment.AlgorithmConfig;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.api.learning.trainer.EpisodeDataMaker;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.api.policy.PolicySupplier;
import vahy.api.predictor.TrainablePredictor;
import vahy.impl.episode.DataPointGeneratorGeneric;

import java.util.List;

public class RunnerArguments<TConfig extends ProblemConfig,
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord,
    TStatistics extends EpisodeStatistics> {

    private String policyId;

    private TConfig problemConfig;
    private SystemConfig systemConfig;
    private AlgorithmConfig algorithmConfig;

    private InitialStateSupplier<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier;
    private EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeResultsFactory;

    private EpisodeStatisticsCalculator<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord, TStatistics> episodeStatisticsCalculator;
    private List<DataPointGeneratorGeneric<TStatistics>> additionalDataPointGeneratorList;

    private TrainablePredictor trainablePredictor;
    private DataAggregator dataAggregator;

//    private SearchNodeMetadataFactory<TAction, TPlayerObservation, TOpponentObservation, TMetadata, TState> searchNodeMetadataFactory;
//    private Supplier<RiskAverseNodeSelector<TAction, TPlayerObservation, TOpponentObservation, TMetadata, TState>> nodeSelectorSupplier;
//    private NodeEvaluator<TAction, TPlayerObservation, TOpponentObservation, TMetadata, TState> evaluator;
    private PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> opponentPolicySupplier;

    private PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policySupplier;
    private EpisodeDataMaker<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> dataMaker;
    private EpisodeWriter<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeWriter;

    public RunnerArguments(String policyId, TConfig problemConfig,
                           SystemConfig systemConfig,
                           AlgorithmConfig algorithmConfig,
                           InitialStateSupplier<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                           EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeResultsFactory,
                           EpisodeStatisticsCalculator<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord, TStatistics> episodeStatisticsCalculator,
                           List<DataPointGeneratorGeneric<TStatistics>> additionalDataPointGeneratorList,
                           TrainablePredictor trainablePredictor,
                           DataAggregator dataAgregator,
                           PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> opponentPolicySupplier,
                           PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policySupplier,
                           EpisodeDataMaker<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> dataMaker,
                           EpisodeWriter<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeWriter) {
        this.policyId = policyId;
        this.problemConfig = problemConfig;
        this.systemConfig = systemConfig;
        this.algorithmConfig = algorithmConfig;
        this.initialStateSupplier = initialStateSupplier;
        this.episodeResultsFactory = episodeResultsFactory;
        this.episodeStatisticsCalculator = episodeStatisticsCalculator;
        this.additionalDataPointGeneratorList = additionalDataPointGeneratorList;
        this.trainablePredictor = trainablePredictor;
        this.dataAggregator = dataAgregator;
        this.opponentPolicySupplier = opponentPolicySupplier;
        this.policySupplier = policySupplier;
        this.dataMaker = dataMaker;
        this.episodeWriter = episodeWriter;
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

    public InitialStateSupplier<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState> getInitialStateSupplier() {
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

    public TrainablePredictor getTrainablePredictor() {
        return trainablePredictor;
    }

    public DataAggregator getDataAggregator() {
        return dataAggregator;
    }

    public PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getOpponentPolicySupplier() {
        return opponentPolicySupplier;
    }

    public PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getPolicySupplier() {
        return policySupplier;
    }

    public EpisodeDataMaker<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getDataMaker() {
        return dataMaker;
    }

    public EpisodeWriter<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getEpisodeWriter() {
        return episodeWriter;
    }

    public String getPolicyId() {
        return policyId;
    }
}
