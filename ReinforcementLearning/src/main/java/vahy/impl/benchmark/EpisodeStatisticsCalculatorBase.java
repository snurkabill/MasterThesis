package vahy.impl.benchmark;

import vahy.api.benchmark.EpisodeStatisticsCalculator;
import vahy.api.episode.EpisodeResults;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.utils.MathStreamUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class EpisodeStatisticsCalculatorBase<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TState extends State<TAction, TObservation, TState>>
    implements EpisodeStatisticsCalculator<TAction, TObservation, TState, EpisodeStatisticsBase> {

    @Override
    public EpisodeStatisticsBase calculateStatistics(List<EpisodeResults<TAction, TObservation, TState>> episodeResultsList, Duration durations) {
        var policyCount = episodeResultsList.get(0).getPolicyCount();
        List<Double> averagePlayerStepCount = new ArrayList<>(policyCount);
        List<Double> stdevPlayerStepCount = new ArrayList<>(policyCount);
        for (int i = 0; i < policyCount; i++) {
            var index = i;
            var average = MathStreamUtils.calculateAverage(episodeResultsList, value -> value.getPlayerStepCountList().get(index));
            averagePlayerStepCount.add(average);
            stdevPlayerStepCount.add(MathStreamUtils.calculateStdev(episodeResultsList, value -> value.getPlayerStepCountList().get(index), average));
        }

        List<Double> averagePlayerDecisionTime = new ArrayList<>(policyCount);
        List<Double> stdevPlayerDecisionTime = new ArrayList<>(policyCount);
        for (int i = 0; i < policyCount; i++) {
            var index = i;
            var average = MathStreamUtils.calculateAverage(episodeResultsList, value -> value.getAverageDurationPerDecision().get(index));
            averagePlayerDecisionTime.add(average);
            stdevPlayerDecisionTime.add(MathStreamUtils.calculateStdev(episodeResultsList, value -> value.getPlayerStepCountList().get(index), average));
        }

        List<Double> totalPayoffAverage = new ArrayList<>(policyCount);
        List<Double> totalPayoffStdev = new ArrayList<>(policyCount);
        for (int i = 0; i < policyCount; i++) {
            var index_i = i;
            var average = MathStreamUtils.calculateAverage(episodeResultsList, x -> x.getTotalPayoff().get(index_i));
            var stdev = MathStreamUtils.calculateStdev(episodeResultsList, x -> x.getTotalPayoff().get(index_i));
            totalPayoffAverage.add(average);
            totalPayoffStdev.add(stdev);
        }
        var averageMillisPerEpisode = MathStreamUtils.calculateAverage(episodeResultsList, (x) -> x.getDuration().toMillis());
        var stdevMillisPerEpisode = MathStreamUtils.calculateStdev(episodeResultsList, (x) -> x.getDuration().toMillis());
        return new EpisodeStatisticsBase(
            durations,
            policyCount,
            averagePlayerStepCount,
            stdevPlayerStepCount,
            averagePlayerDecisionTime,
            stdevPlayerDecisionTime,
            averageMillisPerEpisode,
            stdevMillisPerEpisode,
            totalPayoffAverage,
            totalPayoffStdev);
    }
}
