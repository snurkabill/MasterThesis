package vahy.impl.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.experiment.AlgorithmConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.impl.learning.trainer.Trainer;
import vahy.utils.ImmutableTuple;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class PolicyTrainingCycle<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>,
    TPolicyRecord extends PolicyRecord,
    TStatistics extends EpisodeStatistics> {

    private static Logger logger = LoggerFactory.getLogger(PolicyTrainingCycle.class.getName());

    private final SystemConfig systemConfig;
    private final AlgorithmConfig algorithmConfig;
    private final EpisodeWriter<TAction, TObservation, TState, TPolicyRecord> episodeWriter;
    private final Trainer<TAction, TObservation, TState, TPolicyRecord, TStatistics> trainer;


    public PolicyTrainingCycle(SystemConfig systemConfig,
                               AlgorithmConfig algorithmConfig,
                               EpisodeWriter<TAction, TObservation, TState, TPolicyRecord> episodeWriter,
                               Trainer<TAction, TObservation, TState, TPolicyRecord, TStatistics> trainer) {
        this.systemConfig = systemConfig;
        this.algorithmConfig = algorithmConfig;
        this.episodeWriter = episodeWriter;
        this.trainer = trainer;
    }

    public ImmutableTuple<Duration, List<TStatistics>> startTraining() {
        var startTrainingMillis = System.currentTimeMillis();
        var statisticsList = innerTrainPolicy();
        var endTrainingMillis = System.currentTimeMillis();
        return new ImmutableTuple<>(Duration.ofMillis(endTrainingMillis - startTrainingMillis), statisticsList);
    }

    private List<TStatistics> innerTrainPolicy() {
        var statisticsList = new ArrayList<TStatistics>(algorithmConfig.getStageCount());
        for (int i = 0; i < algorithmConfig.getStageCount(); i++) {
            logger.info("Sampling episodes for [{}]th iteration", i);
            var episodes = trainer.sampleTraining(algorithmConfig.getBatchEpisodeCount());
            if(systemConfig.isEvaluateDuringTraining()) {
                logger.info("Evaluating [{}] episodes without any exploration or noise", algorithmConfig.getBatchEpisodeCount());
                trainer.evaluate(algorithmConfig.getBatchEpisodeCount());
            }
            logger.info("Training predictors");
            trainer.trainPredictors(episodes.getFirst());
            trainer.makeLog();
            statisticsList.add(episodes.getSecond());
            if(systemConfig.dumpTrainingData()) {
                episodeWriter.writeTrainingEpisode(i, episodes.getFirst());
            }
        }
        return statisticsList;
    }
}
