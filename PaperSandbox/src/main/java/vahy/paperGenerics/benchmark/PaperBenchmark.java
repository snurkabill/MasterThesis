package vahy.paperGenerics.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.EpisodeResults;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyMode;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.experiment.CalculatedResultStatistics;
import vahy.paperGenerics.experiment.PaperPolicyResults;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.PaperPolicyRecord;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.reinforcement.episode.PaperEpisodeResults;
import vahy.paperGenerics.reinforcement.episode.PaperEpisodeResultsFactory;
import vahy.paperGenerics.reinforcement.episode.PaperRolloutGameSampler;
import vahy.utils.MathStreamUtils;
import vahy.vizualiation.ProgressTrackerSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PaperBenchmark<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PaperPolicyRecord> {

    private static final Logger logger = LoggerFactory.getLogger(PaperBenchmark.class.getName());

    private final List<PaperBenchmarkingPolicy<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> benchmarkingPolicyList;
    private final PaperPolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> environmentPolicySupplier;
    private final InitialStateSupplier<TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier;
    private final ProgressTrackerSettings progressTrackerSettings;
    private final PaperEpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, PaperPolicyRecord> resultsFactory;


    public PaperBenchmark(List<PaperBenchmarkingPolicy<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> benchmarkingPolicyList,
                          PaperPolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> environmentPolicySupplier,
                          InitialStateSupplier<TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                          PaperEpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, PaperPolicyRecord> resultsFactory,
                          ProgressTrackerSettings progressTrackerSettings) {
        this.benchmarkingPolicyList = benchmarkingPolicyList;
        this.environmentPolicySupplier = environmentPolicySupplier;
        this.initialStateSupplier = initialStateSupplier;
        this.resultsFactory = resultsFactory;
        this.progressTrackerSettings = progressTrackerSettings;
    }

    public List<PaperPolicyResults<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState, TPolicyRecord>> runBenchmark(int episodeCount, int stepCountLimit, int threadCount) {
        logger.info("Running benchmark for [{}] iterations", episodeCount);
        var results = new ArrayList<PaperPolicyResults<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState, TPolicyRecord>>();
        for (PaperBenchmarkingPolicy<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> benchmarkingPolicy : benchmarkingPolicyList) {
            logger.info("Starting benchmark for policy [{}]", benchmarkingPolicy.getPolicyName());
            var gameSampler = new PaperRolloutGameSampler<>(
                initialStateSupplier,
                resultsFactory,
                benchmarkingPolicy.getPolicySupplier(),
                environmentPolicySupplier,
                PolicyMode.INFERENCE,
                progressTrackerSettings,
                threadCount);
            long start = System.currentTimeMillis();
            var episodeResults = gameSampler.sampleEpisodes(episodeCount, stepCountLimit);
            long end = System.currentTimeMillis();
            var episodeList = episodeResults.stream().map(x -> (PaperEpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>) x).collect(Collectors.toList()); // TODO: remove casting
            logger.info("Benchmarking [{}] policy took [{}] nanosecond", benchmarkingPolicy.getPolicyName(), end - start);
            results.add(new PaperPolicyResults<>(benchmarkingPolicy, episodeList, calculateStatistics(episodeList), (end - start)));
        }
        return results;
    }

    private CalculatedResultStatistics calculateStatistics(List<PaperEpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> episodeList) {
        var riskHitCounter = episodeList.stream().filter(PaperEpisodeResults::isRiskHit).count();
        var riskHitRatio = riskHitCounter / (double) episodeList.size();
        var averagePlayerStepCount = MathStreamUtils.calculateAverage(episodeList, EpisodeResults::getPlayerStepCount);
        var stdevPlayerStepCount = MathStreamUtils.calculateStdev(episodeList, EpisodeResults::getPlayerStepCount);
        var totalPayoffAverage = MathStreamUtils.calculateAverage(episodeList, EpisodeResults::getTotalPayoff);
        var totalPayoffStdev = MathStreamUtils.calculateStdev(episodeList, EpisodeResults::getTotalPayoff, totalPayoffAverage);
        var averageMillisPerEpisode = MathStreamUtils.calculateAverage(episodeList, (x) -> x.getDuration().toMillis());
        return new CalculatedResultStatistics(averagePlayerStepCount, stdevPlayerStepCount, averageMillisPerEpisode, totalPayoffAverage, totalPayoffStdev, riskHitCounter, riskHitRatio);
    }
}
