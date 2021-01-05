package vahy.impl.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.learning.trainer.EarlyStoppingStrategy;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.impl.learning.trainer.Trainer;
import vahy.utils.ImmutableTuple;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class PolicyTrainingCycle<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TState extends State<TAction, TObservation, TState>,
    TStatistics extends EpisodeStatistics> {

    private static Logger logger = LoggerFactory.getLogger(PolicyTrainingCycle.class.getName());

    private final SystemConfig systemConfig;
    private final CommonAlgorithmConfig algorithmConfig;
    private final EpisodeWriter<TAction, TObservation, TState> episodeWriter;
    private final Trainer<TAction, TObservation, TState, TStatistics> trainer;
    private final EarlyStoppingStrategy<TAction, TObservation, TState, TStatistics> earlyStoppingStrategy;


    public PolicyTrainingCycle(SystemConfig systemConfig,
                               CommonAlgorithmConfig algorithmConfig,
                               EpisodeWriter<TAction, TObservation, TState> episodeWriter,
                               Trainer<TAction, TObservation, TState, TStatistics> trainer,
                               EarlyStoppingStrategy<TAction, TObservation, TState, TStatistics> earlyStoppingStrategy) {
        this.systemConfig = systemConfig;
        this.algorithmConfig = algorithmConfig;
        this.episodeWriter = episodeWriter;
        this.trainer = trainer;
        this.earlyStoppingStrategy = earlyStoppingStrategy;
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
            var trainingEpisodes = trainer.sampleTraining(algorithmConfig.getBatchEpisodeCount());
            if(systemConfig.isEvaluateDuringTraining()) {
                logger.info("Evaluating [{}] episodes without any exploration or noise", systemConfig.getEvalEpisodeCountDuringTraining());
                var evaluationEpisodes = trainer.evaluate(systemConfig.getEvalEpisodeCountDuringTraining());
                if (earlyStoppingStrategy.isStoppingConditionFulfilled(trainingEpisodes, evaluationEpisodes)) {
                    logger.info("Early stopping at i=[{}] iteration", i);
                    return statisticsList;
                }
            } else if (earlyStoppingStrategy.isStoppingConditionFulfilled(trainingEpisodes)) {
                logger.info("Early stopping at i=[{}] iteration", i);
                return statisticsList;
            }
            logger.info("Training predictors");
            trainer.trainPredictors(trainingEpisodes.getFirst());
            trainer.makeLog();
            statisticsList.add(trainingEpisodes.getSecond());
            if(systemConfig.dumpTrainingData()) {
                episodeWriter.writeTrainingEpisode(i, trainingEpisodes.getFirst());
            }
        }
        return statisticsList;
    }
}
