package vahy.impl.experiment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.experiment.AlgorithmConfig;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.learning.trainer.EpisodeDataMaker;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecord;
import vahy.api.policy.PolicySupplier;
import vahy.api.predictor.TrainablePredictor;
import vahy.impl.episode.DataPointGeneratorGeneric;
import vahy.impl.learning.trainer.GameSamplerImpl;
import vahy.impl.learning.trainer.Trainer;
import vahy.vizualiation.ProgressTrackerSettings;

import java.time.Duration;
import java.util.List;

public class AbstractExperiment<
    TConfig extends ProblemConfig,
    TAction extends Enum<TAction> & Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    private final Logger logger = LoggerFactory.getLogger(AbstractExperiment.class);

    public void run(PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policySupplier,
                    PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> opponentPolicySupplier,
                    EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeResultsFactory,
                    InitialStateSupplier<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                    ProgressTrackerSettings progressTrackerSettings,
                    List<DataPointGeneratorGeneric<List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>>>> additionalDataPointGeneratorList,
                    TrainablePredictor trainablePredictor,
                    EpisodeDataMaker<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeDataMaker,
                    EpisodeWriter<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeWriter,
                    SystemConfig systemConfig,
                    AlgorithmConfig algorithmConfig)
    {

        var trainer = getAbstractTrainer(
            policySupplier,
            opponentPolicySupplier,
            episodeResultsFactory,
            initialStateSupplier,
            episodeDataMaker,
            additionalDataPointGeneratorList,
            trainablePredictor,
            algorithmConfig,
            systemConfig,
            progressTrackerSettings
        );

        var trainingCycle = new PolicyTrainingCycle<>(
            systemConfig,
            algorithmConfig,
            episodeWriter,
            trainer);

        Duration trainingDuration = trainingCycle.startTraining();
    }

    private Trainer<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getAbstractTrainer(
        PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policySupplier,
        PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> opponentPolicySupplier,
        EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeResultsFactory,
        InitialStateSupplier<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
        EpisodeDataMaker<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeDataMaker,
        List<DataPointGeneratorGeneric<List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>>>> additionalDataPointGeneratorList,
        TrainablePredictor predictor,
        AlgorithmConfig algorithmConfig,
        SystemConfig systemConfig,
        ProgressTrackerSettings progressTrackerSettings
        )
    {
        var trainerAlgorithm = algorithmConfig.getDataAggregationAlgorithm();
        var gameSampler = new GameSamplerImpl<>(
            initialStateSupplier,
            episodeResultsFactory,
            PolicyMode.TRAINING,
            progressTrackerSettings,
            systemConfig.getParallelThreadsCount(),
            policySupplier,
            opponentPolicySupplier,
            additionalDataPointGeneratorList
            );

        var dataAggregator = trainerAlgorithm.resolveDataAggregator(algorithmConfig);
        return new Trainer<>(predictor, gameSampler, dataAggregator, episodeDataMaker, progressTrackerSettings);
    }


}
