package vahy.impl.experiment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.experiment.AlgorithmConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.impl.learning.trainer.Trainer;

import java.time.Duration;

public class PolicyTrainingCycle<
    TAction extends Enum<TAction> & Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    private static Logger logger = LoggerFactory.getLogger(PolicyTrainingCycle.class.getName());

    private final SystemConfig systemConfig;
    private final AlgorithmConfig algorithmConfig;
    private final EpisodeWriter<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeWriter;
    private final Trainer<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> trainer;


    public PolicyTrainingCycle(SystemConfig systemConfig,
                               AlgorithmConfig algorithmConfig,
                               EpisodeWriter<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeWriter,
                               Trainer<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> trainer) {
        this.systemConfig = systemConfig;
        this.algorithmConfig = algorithmConfig;
        this.episodeWriter = episodeWriter;
        this.trainer = trainer;
    }

    public Duration executeTrainingPolicy() {
        var startTrainingMillis = System.currentTimeMillis();
        innerTrainPolicy();
        var endTrainingMillis = System.currentTimeMillis();
        return Duration.ofMillis(endTrainingMillis - startTrainingMillis);
    }

    private void innerTrainPolicy() {
        for (int i = 0; i < algorithmConfig.getStageCount(); i++) {
            logger.info("Training policy for [{}]th iteration", i);
            var episodes = trainer.trainPolicy(algorithmConfig.getBatchEpisodeCount(), algorithmConfig.getMaximalStepCountBound());
            if(systemConfig.dumpTrainingData()) {
                episodeWriter.writeTrainingEpisode(i, episodes);
            }
        }
    }

}
