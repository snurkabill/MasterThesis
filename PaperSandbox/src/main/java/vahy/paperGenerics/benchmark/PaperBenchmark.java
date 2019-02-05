package vahy.paperGenerics.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.reinforcement.episode.EpisodeGameSampler;
import vahy.paperGenerics.reinforcement.episode.EpisodeResults;

import java.util.ArrayList;
import java.util.List;

public class PaperBenchmark<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> {

    private static final Logger logger = LoggerFactory.getLogger(PaperBenchmark.class.getName());

    private final List<PaperBenchmarkingPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> benchmarkingPolicyList;
    private final PaperPolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> environmentPolicySupplier;
    private final InitialStateSupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier;

    public PaperBenchmark(List<PaperBenchmarkingPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> benchmarkingPolicyList,
                          PaperPolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> environmentPolicySupplier,
                          InitialStateSupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier) {
        this.benchmarkingPolicyList = benchmarkingPolicyList;
        this.environmentPolicySupplier = environmentPolicySupplier;
        this.initialStateSupplier = initialStateSupplier;
    }

    public List<PaperPolicyResults<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> runBenchmark(int episodeCount,
                                                                                                                                          int stepCountLimit) {
        logger.info("Running benchmark for [{}] iterations", episodeCount);
        List<PaperPolicyResults<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> results = new ArrayList<>();
        for (PaperBenchmarkingPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> benchmarkingPolicy : benchmarkingPolicyList) {
            logger.info("Starting benchmark for policy [{}]", benchmarkingPolicy.getPolicyName());
            EpisodeGameSampler<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> gameSampler = new EpisodeGameSampler<>(
                initialStateSupplier,
                benchmarkingPolicy.getPolicySupplier(),
                environmentPolicySupplier,
                stepCountLimit);
            long start = System.nanoTime();
            List<EpisodeResults<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> resultList = gameSampler.sampleEpisodes(episodeCount);
            long end = System.nanoTime();
            logger.info("Benchmarking [{}] policy took [{}] nanosecond", benchmarkingPolicy.getPolicyName(), end - start);
            results.add(new PaperPolicyResults<>(benchmarkingPolicy, resultList, (end - start) / (double) episodeCount));
        }
        return results;
    }
}
