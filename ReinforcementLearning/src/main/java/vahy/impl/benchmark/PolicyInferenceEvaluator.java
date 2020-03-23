package vahy.impl.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.benchmark.EpisodeStatisticsCalculator;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.experiment.ProblemConfig;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecord;
import vahy.api.policy.PolicySupplier;
import vahy.impl.learning.trainer.GameSamplerImpl;

import java.time.Duration;

public class PolicyInferenceEvaluator<
    TConfig extends ProblemConfig,
    TAction extends Enum<TAction> & Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord,
    TStatistics extends EpisodeStatistics> {

    private static final Logger logger = LoggerFactory.getLogger(PolicyInferenceEvaluator.class.getName());

    private final BenchmarkedPolicy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policy;
    private final PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> environmentPolicySupplier;
    private final InitialStateSupplier<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier;
    private final EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> resultsFactory;
    private final EpisodeStatisticsCalculator<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord, TStatistics> episodeStatisticsCalculator;

    public PolicyInferenceEvaluator(BenchmarkedPolicy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policy,
                                    PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> environmentPolicySupplier,
                                    InitialStateSupplier<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                                    EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> resultsFactory,
                                    EpisodeStatisticsCalculator<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord, TStatistics> episodeStatisticsCalculator) {
        this.policy = policy;
        this.environmentPolicySupplier = environmentPolicySupplier;
        this.initialStateSupplier = initialStateSupplier;
        this.resultsFactory = resultsFactory;
        this.episodeStatisticsCalculator = episodeStatisticsCalculator;
    }

    public PolicyResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord, TStatistics> runInferenceEvaluation(int episodeCount, int stepCountLimit, int threadCount) {
        logger.info("Running evaluation inference of [{}] policy for [{}] iterations", policy.getPolicyName(), episodeCount);
        var gameSampler = new GameSamplerImpl<>(
            initialStateSupplier,
            resultsFactory,
            PolicyMode.INFERENCE,
            threadCount,
            policy.getPolicySupplier(),
            environmentPolicySupplier);
        long start = System.currentTimeMillis();
        var episodeList = gameSampler.sampleEpisodes(episodeCount, stepCountLimit);
        long end = System.currentTimeMillis();
        logger.info("Evaluation of [{}] policy took [{}] milliseconds", policy.getPolicyName(), end - start);
        return new PolicyResults<>(policy, episodeList, episodeStatisticsCalculator.calculateStatistics(episodeList), Duration.ofMillis(end - start));
    }

}
