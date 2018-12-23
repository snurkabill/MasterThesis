package vahy.paperGenerics.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.environment.state.PaperState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.reinforcement.episode.EpisodeGameSampler;
import vahy.paperGenerics.reinforcement.episode.EpisodeResults;

import java.util.ArrayList;
import java.util.List;

public class PaperBenchmark<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TObservation extends DoubleVector,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TObservation, TState>> {

    private static final Logger logger = LoggerFactory.getLogger(PaperBenchmark.class.getName());

    private final List<PaperBenchmarkingPolicy<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> benchmarkingPolicyList;
    private final PaperPolicySupplier<TAction, TReward, TObservation, TSearchNodeMetadata, TState> environmentPolicySupplier;
    private final InitialStateSupplier<TAction, TReward, TObservation, TState> initialStateSupplier;

    public PaperBenchmark(List<PaperBenchmarkingPolicy<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> benchmarkingPolicyList,
                          PaperPolicySupplier<TAction, TReward, TObservation, TSearchNodeMetadata, TState> environmentPolicySupplier,
                          InitialStateSupplier<TAction, TReward, TObservation, TState> initialStateSupplier) {
        this.benchmarkingPolicyList = benchmarkingPolicyList;
        this.environmentPolicySupplier = environmentPolicySupplier;
        this.initialStateSupplier = initialStateSupplier;
    }

    public List<PaperPolicyResults<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> runBenchmark(int uniqueEpisodeCount, int episodeCount, int stepCountLimit) {
        logger.info("Running benchmark with [{}] unique episodes each for [{}] iterations", uniqueEpisodeCount, episodeCount);
        int totalEpisodeCount = uniqueEpisodeCount * episodeCount;
        List<PaperPolicyResults<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> results = new ArrayList<>();
        for (PaperBenchmarkingPolicy<TAction, TReward, TObservation, TSearchNodeMetadata, TState> benchmarkingPolicy : benchmarkingPolicyList) {
            logger.info("Starting benchmark for policy [{}]", benchmarkingPolicy.getPolicyName());
            EpisodeGameSampler<TAction, TReward, TObservation, TSearchNodeMetadata, TState> gameSampler = new EpisodeGameSampler<>(
                initialStateSupplier,
                benchmarkingPolicy.getPolicySupplier(),
                environmentPolicySupplier,
                stepCountLimit);
            long start = System.nanoTime();
            List<EpisodeResults<TAction, TReward, TObservation, TState>> resultList = gameSampler.sampleEpisodes(totalEpisodeCount);
            long end = System.nanoTime();
            logger.info("Benchmarking [{}] policy took [{}] nanosecond", benchmarkingPolicy.getPolicyName(), end - start);
            results.add(new PaperPolicyResults<>(benchmarkingPolicy, resultList, (end - start) / (double) totalEpisodeCount));
        }
        return results;
    }
}
