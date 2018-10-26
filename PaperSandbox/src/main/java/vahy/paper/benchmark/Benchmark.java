package vahy.paper.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.paper.policy.EnvironmentPolicySupplier;
import vahy.paper.reinforcement.EpisodeAggregator;
import vahy.paper.reinforcement.episode.PaperEpisode;

import java.util.ArrayList;
import java.util.List;

public class Benchmark {

    private static final Logger logger = LoggerFactory.getLogger(Benchmark.class.getName());

    private final List<BenchmarkingPolicy> benchmarkingPolicyList;
    private final EnvironmentPolicySupplier environmentPolicySupplier;
    private final HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier;

    public Benchmark(List<BenchmarkingPolicy> benchmarkingPolicyList,
                     EnvironmentPolicySupplier environmentPolicySupplier,
                     HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier) {
        this.benchmarkingPolicyList = benchmarkingPolicyList;
        this.environmentPolicySupplier = environmentPolicySupplier;
        this.hallwayGameInitialInstanceSupplier = hallwayGameInitialInstanceSupplier;
    }

    public List<PolicyResults> runBenchmark(int uniqueEpisodeCount, int episodeCount) {
        logger.info("Running benchmark with [{}] unique episodes each for [{}] iterations", uniqueEpisodeCount, episodeCount);
        int totalEpisodeCount = uniqueEpisodeCount * episodeCount;
        List<PolicyResults> results = new ArrayList<>();
        for (BenchmarkingPolicy benchmarkingPolicy : benchmarkingPolicyList) {
            logger.info("Starting benchmark for policy [{}]", benchmarkingPolicy.getPolicyName());
            EpisodeAggregator episodeAggregator = new EpisodeAggregator(
                uniqueEpisodeCount,
                episodeCount,
                hallwayGameInitialInstanceSupplier,
                benchmarkingPolicy.getPolicySupplier(),
                environmentPolicySupplier
            );
            long start = System.nanoTime();
            List<PaperEpisode> calculatedEpisodes = episodeAggregator.runSimulation();
            long end = System.nanoTime();
            logger.info("Benchmarking [{}] policy took [{}] nanosecond", benchmarkingPolicy.getPolicyName(), end - start);
            results.add(new PolicyResults(benchmarkingPolicy, calculatedEpisodes, (end - start) / (double) totalEpisodeCount));
        }
        return results;
    }

}
