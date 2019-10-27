package vahy.api.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecord;
import vahy.api.policy.PolicySupplier;
import vahy.impl.learning.trainer.GameSamplerImpl;
import vahy.impl.model.observation.DoubleVector;
import vahy.vizualiation.ProgressTrackerSettings;

import java.util.ArrayList;
import java.util.List;

public class PolicyBenchmark<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    private static final Logger logger = LoggerFactory.getLogger(PolicyBenchmark.class.getName());

    private final List<BenchmarkedPolicy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> policyList;
    private final PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> environmentPolicySupplier;
    private final InitialStateSupplier<TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier;
    private final ProgressTrackerSettings progressTrackerSettings;
    private final EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> resultsFactory;
    private final EpisodeStatisticsCalculator<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeStatisticsCalculator;


    public PolicyBenchmark(List<BenchmarkedPolicy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> policyList,
                           PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> environmentPolicySupplier,
                           InitialStateSupplier<TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                           EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> resultsFactory,
                           EpisodeStatisticsCalculator<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeStatisticsCalculator,
                           ProgressTrackerSettings progressTrackerSettings) {
        this.policyList = policyList;
        this.environmentPolicySupplier = environmentPolicySupplier;
        this.initialStateSupplier = initialStateSupplier;
        this.resultsFactory = resultsFactory;
        this.progressTrackerSettings = progressTrackerSettings;
        this.episodeStatisticsCalculator = episodeStatisticsCalculator;
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
                environmentPolicySupplier);
            long start = System.currentTimeMillis();
            var episodeList = gameSampler.sampleEpisodes(episodeCount, stepCountLimit);
            long end = System.currentTimeMillis();
            logger.info("Benchmarking [{}] policy took [{}] nanosecond", benchmarkingPolicy.getPolicyName(), end - start);
            results.add(new PolicyResults<>(benchmarkingPolicy, episodeList, episodeStatisticsCalculator.calculateStatistics(episodeList), (end - start)));
        }
        return results;
    }

}
