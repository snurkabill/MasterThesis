package vahy.impl.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import vahy.impl.episode.FromEpisodesDataPointGeneratorGeneric;
import vahy.impl.learning.trainer.GameSamplerImpl;
import vahy.vizualiation.ProgressTrackerSettings;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class PolicyBenchmark<
    TConfig extends ProblemConfig,
    TAction extends Enum<TAction> & Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    private static final Logger logger = LoggerFactory.getLogger(PolicyBenchmark.class.getName());

    private final List<BenchmarkedPolicy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> policyList;
    private final PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> environmentPolicySupplier;
    private final InitialStateSupplier<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier;
    private final ProgressTrackerSettings progressTrackerSettings;
    private final EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> resultsFactory;
    private final EpisodeStatisticsCalculator<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeStatisticsCalculator;
    private final List<FromEpisodesDataPointGeneratorGeneric<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> additionalDataPointGeneratorList;

    public PolicyBenchmark(List<BenchmarkedPolicy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> policyList,
                           PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> environmentPolicySupplier,
                           InitialStateSupplier<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                           EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> resultsFactory,
                           EpisodeStatisticsCalculator<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeStatisticsCalculator,
                           ProgressTrackerSettings progressTrackerSettings,
                           List<FromEpisodesDataPointGeneratorGeneric<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> additionalDataPointGeneratorList) {
        this.policyList = policyList;
        this.environmentPolicySupplier = environmentPolicySupplier;
        this.initialStateSupplier = initialStateSupplier;
        this.resultsFactory = resultsFactory;
        this.progressTrackerSettings = progressTrackerSettings;
        this.episodeStatisticsCalculator = episodeStatisticsCalculator;
        this.additionalDataPointGeneratorList = additionalDataPointGeneratorList;
    }

    public List<PolicyResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> runBenchmark(
        int episodeCount,
        int stepCountLimit,
        int threadCount)
    {
        logger.info("Running benchmark for [{}] iterations", episodeCount);
        var results = new ArrayList<PolicyResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>>();
        for (var benchmarkingPolicy : policyList) {
            logger.info("Starting benchmark for policy [{}]", benchmarkingPolicy.getPolicyName());
            var gameSampler = new GameSamplerImpl<>(
                initialStateSupplier,
                resultsFactory,
                PolicyMode.INFERENCE,
                progressTrackerSettings,
                threadCount,
                benchmarkingPolicy.getPolicySupplier(),
                environmentPolicySupplier,
                additionalDataPointGeneratorList);
            long start = System.currentTimeMillis();
            var episodeList = gameSampler.sampleEpisodes(episodeCount, stepCountLimit);
            long end = System.currentTimeMillis();
            logger.info("Benchmarking [{}] policy took [{}] milliseconds", benchmarkingPolicy.getPolicyName(), end - start);
            results.add(new PolicyResults<>(benchmarkingPolicy, episodeList, episodeStatisticsCalculator.calculateStatistics(episodeList), Duration.ofMillis(end - start)));
        }
        return results;
    }

}
