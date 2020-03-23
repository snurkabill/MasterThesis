package vahy.impl.experiment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.benchmark.EpisodeStatistics;
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
import vahy.api.policy.PolicyRecord;
import vahy.api.policy.PolicySupplier;
import vahy.api.predictor.TrainablePredictor;
import vahy.impl.episode.DataPointGeneratorGeneric;
import vahy.vizualiation.ProgressTrackerSettings;

import java.util.List;

public class AbstractExperiment<
    TConfig extends ProblemConfig,
    TAction extends Enum<TAction> & Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord,
    TStatistics extends EpisodeStatistics> {

    private final Logger logger = LoggerFactory.getLogger(AbstractExperiment.class);

    public List<EpisodeStatistics> run(PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policySupplier,
                                       PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> opponentPolicySupplier,
                                       EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeResultsFactory,
                                       InitialStateSupplier<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                                       ProgressTrackerSettings progressTrackerSettings,
                                       List<DataPointGeneratorGeneric<List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>>>> additionalDataPointGeneratorList,
                                       TrainablePredictor trainablePredictor,
                                       EpisodeDataMaker<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeDataMaker,
                                       EpisodeWriter<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeWriter,
                                       SystemConfig systemConfig,
                                       ProblemConfig problemConfig,
                                       AlgorithmConfig algorithmConfig)
    {

        return null;
    }
}
