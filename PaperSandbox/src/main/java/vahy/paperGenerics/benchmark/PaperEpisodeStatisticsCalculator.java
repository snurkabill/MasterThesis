package vahy.paperGenerics.benchmark;

import vahy.api.benchmark.EpisodeStatisticsCalculator;
import vahy.api.episode.EpisodeResults;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.PaperPolicyRecord;
import vahy.utils.MathStreamUtils;

import java.time.Duration;
import java.util.List;

public class PaperEpisodeStatisticsCalculator<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PaperPolicyRecord>
    implements EpisodeStatisticsCalculator<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord, PaperEpisodeStatistics> {

    @Override
    public PaperEpisodeStatistics calculateStatistics(List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> episodeResultsList, Duration duration) {
        var averagePlayerStepCount = MathStreamUtils.calculateAverage(episodeResultsList, EpisodeResults::getPlayerStepCount);
        var stdevPlayerStepCount = MathStreamUtils.calculateStdev(episodeResultsList, EpisodeResults::getPlayerStepCount);
        var totalPayoffAverage = MathStreamUtils.calculateAverage(episodeResultsList, EpisodeResults::getTotalPayoff);
        var totalPayoffStdev = MathStreamUtils.calculateStdev(episodeResultsList, EpisodeResults::getTotalPayoff, totalPayoffAverage);
        var averageMillisPerEpisode = MathStreamUtils.calculateAverage(episodeResultsList, (x) -> x.getDuration().toMillis());
        var stdevMillisPerEpisode = MathStreamUtils.calculateStdev(episodeResultsList, (x) -> x.getDuration().toMillis());
        var riskHitCounter = episodeResultsList.stream().filter(x -> x.getFinalState().isRiskHit()).count();
        var riskHitRatio = riskHitCounter / (double) episodeResultsList.size();
        var riskHitStdev = MathStreamUtils.calculateStdev(episodeResultsList, x -> x.getFinalState().isRiskHit() ? 1.0 : 0.0);
        return new PaperEpisodeStatistics(duration, averagePlayerStepCount, stdevPlayerStepCount, averageMillisPerEpisode, stdevMillisPerEpisode, totalPayoffAverage, totalPayoffStdev, riskHitCounter, riskHitRatio, riskHitStdev);
    }
}
