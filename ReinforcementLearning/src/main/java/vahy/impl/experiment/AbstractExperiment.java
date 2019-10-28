package vahy.impl.experiment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.experiment.AlgorithmConfig;
import vahy.api.learning.dataAggregator.DataAggregationAlgorithm;
import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.api.policy.PolicySupplier;
import vahy.api.experiment.SystemConfig;
import vahy.impl.learning.dataAggregator.EveryVisitMonteCarloDataAggregator;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.dataAggregator.ReplayBufferDataAggregator;
import vahy.impl.learning.trainer.AbstractTrainer;
import vahy.utils.EnumUtils;
import vahy.vizualiation.ProgressTrackerSettings;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public abstract class AbstractExperiment<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    private static Logger logger = LoggerFactory.getLogger(AbstractExperiment.class.getName());

    protected final ProgressTrackerSettings progressTrackerSettings;
    protected final InitialStateSupplier<TAction, TPlayerObservation, TOpponentObservation, TState> instanceSupplier;
    protected final PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policySupplier;
    protected final SystemConfig systemConfig;
    protected final AlgorithmConfig algorithmConfig;
    private final EpisodeWriter<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeWriter;


    protected AbstractExperiment(ProgressTrackerSettings progressTrackerSettings,
                                 InitialStateSupplier<TAction, TPlayerObservation, TOpponentObservation, TState> instanceSupplier,
                                 PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policySupplier,
                                 SystemConfig systemConfig,
                                 AlgorithmConfig algorithmConfig,
                                 EpisodeWriter<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeWriter) {
        this.progressTrackerSettings = progressTrackerSettings;
        this.instanceSupplier = instanceSupplier;
        this.policySupplier = policySupplier;
        this.systemConfig = systemConfig;
        this.algorithmConfig = algorithmConfig;
        this.episodeWriter = episodeWriter;
    }

    protected abstract AbstractTrainer getTrainer();

    public Duration trainPolicy() {
        var trainer = getTrainer();
        var startTrainingMillis = System.currentTimeMillis();
        innerTrainPolicy(trainer);
        var endTrainingMillis = System.currentTimeMillis();
        var trainingDurationMillis = endTrainingMillis - startTrainingMillis;
        return Duration.ofMillis(trainingDurationMillis);
    }

    private void innerTrainPolicy(AbstractTrainer<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> trainer) {
        for (int i = 0; i < algorithmConfig.getStageCount(); i++) {
            logger.info("Training policy for [{}]th iteration", i);
            var episodes = trainer.trainPolicy(algorithmConfig.getBatchEpisodeCount(), algorithmConfig.getMaximalStepCountBound());
            if(systemConfig.dumpTrainingData()) {
                episodeWriter.writeTrainingEpisode(i, episodes);
            }
        }
    }

    private DataAggregator resolveDataAggerator(DataAggregationAlgorithm trainerAlgorithm) {
        switch(trainerAlgorithm) {
            case REPLAY_BUFFER:
                return new ReplayBufferDataAggregator(algorithmConfig.getReplayBufferSize(), new LinkedList<>());
            case FIRST_VISIT_MC:
                return new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
            case EVERY_VISIT_MC:
                return new EveryVisitMonteCarloDataAggregator(new LinkedHashMap<>());
            default: throw EnumUtils.createExceptionForNotExpectedEnumValue(trainerAlgorithm);
        }
    }
}
