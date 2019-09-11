package vahy.paperGenerics.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.experiment.CalculatedResultStatistics;
import vahy.paperGenerics.experiment.PaperPolicyResults;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.reinforcement.episode.sampler.EpisodeGameSampler;
import vahy.paperGenerics.reinforcement.episode.EpisodeResults;
import vahy.utils.MathStreamUtils;
import vahy.vizualiation.ProgressTrackerSettings;

import java.util.ArrayList;
import java.util.List;

public class PaperBenchmark<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    private static final Logger logger = LoggerFactory.getLogger(PaperBenchmark.class.getName());

    private final List<PaperBenchmarkingPolicy<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> benchmarkingPolicyList;
    private final PaperPolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> environmentPolicySupplier;
    private final InitialStateSupplier<TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier;
    private final ProgressTrackerSettings progressTrackerSettings;

    public PaperBenchmark(List<PaperBenchmarkingPolicy<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> benchmarkingPolicyList,
                          PaperPolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> environmentPolicySupplier,
                          InitialStateSupplier<TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                          ProgressTrackerSettings progressTrackerSettings) {
        this.benchmarkingPolicyList = benchmarkingPolicyList;
        this.environmentPolicySupplier = environmentPolicySupplier;
        this.initialStateSupplier = initialStateSupplier;
        this.progressTrackerSettings = progressTrackerSettings;
    }

    public List<PaperPolicyResults<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> runBenchmark(int episodeCount, int stepCountLimit, int threadCount) {
        logger.info("Running benchmark for [{}] iterations", episodeCount);
        List<PaperPolicyResults<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> results = new ArrayList<>();
        for (PaperBenchmarkingPolicy<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> benchmarkingPolicy : benchmarkingPolicyList) {
            logger.info("Starting benchmark for policy [{}]", benchmarkingPolicy.getPolicyName());
            EpisodeGameSampler<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> gameSampler = new EpisodeGameSampler<>(
                initialStateSupplier,
                benchmarkingPolicy.getPolicySupplier(),
                environmentPolicySupplier,
                progressTrackerSettings,
                stepCountLimit,
                threadCount);
            long start = System.currentTimeMillis();
            List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState>> resultList = gameSampler.sampleEpisodes(episodeCount);
            long end = System.currentTimeMillis();
            logger.info("Benchmarking [{}] policy took [{}] nanosecond", benchmarkingPolicy.getPolicyName(), end - start);
            results.add(new PaperPolicyResults<>(benchmarkingPolicy, resultList, calculateStatistics(resultList), (end - start)));
        }
        return results;
    }

    private CalculatedResultStatistics calculateStatistics(List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState>> episodeList) {
        var riskHitCounter = episodeList.stream().filter(EpisodeResults::isRiskHit).count();
        var riskHitRatio = riskHitCounter / (double) episodeList.size();
        var averagePlayerStepCount = MathStreamUtils.calculateAverage(episodeList, EpisodeResults::getPlayerStepCount);
        var stdevPlayerStepCount = MathStreamUtils.calculateStdev(episodeList, EpisodeResults::getPlayerStepCount);
        var totalPayoffAverage = MathStreamUtils.calculateAverage(episodeList, EpisodeResults::getTotalPayoff);
        var totalPayoffStdev = MathStreamUtils.calculateStdev(episodeList, EpisodeResults::getTotalPayoff, totalPayoffAverage);
        var averageMillisPerEpisode = MathStreamUtils.calculateAverage(episodeList, EpisodeResults::getMillisecondDuration);
        return new CalculatedResultStatistics(averagePlayerStepCount, stdevPlayerStepCount, averageMillisPerEpisode, totalPayoffAverage, totalPayoffStdev, riskHitCounter, riskHitRatio);
    }
}
