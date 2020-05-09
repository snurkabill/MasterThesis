package vahy.impl.benchmark;

import vahy.api.benchmark.EpisodeStatisticsCalculator;
import vahy.api.episode.EpisodeResults;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.policy.PolicyRecord;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.MathStreamUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class EpisodeStatisticsCalculatorBase<
    TAction extends Enum<TAction> & Action,
    TObservation extends DoubleVector,
    TState extends State<TAction, TObservation, TState>,
    TPolicyRecord extends PolicyRecord>
    implements EpisodeStatisticsCalculator<TAction, TObservation, TState, TPolicyRecord, EpisodeStatisticsBase> {

    @Override
    public EpisodeStatisticsBase calculateStatistics(List<EpisodeResults<TAction, TObservation, TState, TPolicyRecord>> episodeResultsList, Duration durations) {
        var policyCount = episodeResultsList.get(0).getPolicyCount();
        List<Double> averagePlayerStepCount = new ArrayList<>(policyCount);
        List<Double> stdevPlayerStepCount = new ArrayList<>(policyCount);
        for (int i = 0; i < policyCount; i++) {
            var index = i;
            var average = MathStreamUtils.calculateAverage(episodeResultsList, value -> value.getPlayerStepCountList().get(index));
            averagePlayerStepCount.add(average);
            stdevPlayerStepCount.add(MathStreamUtils.calculateStdev(episodeResultsList, value -> value.getPlayerStepCountList().get(index), average));
        }

        List<List<Double>> totalPayoffAverage = new ArrayList<>(policyCount);
        List<List<Double>> totalPayoffStdev = new ArrayList<>(policyCount);
        for (int i = 0; i < policyCount; i++) {
            var payoffCount = episodeResultsList.get(0).getTotalPayoff().get(i).size();
            totalPayoffAverage.add(new ArrayList<>(payoffCount));
            totalPayoffStdev.add(new ArrayList<>(payoffCount));
            for (int j = 0; j < payoffCount; j++) {
                var index_i = i;
                var index_j = j;
                var average = MathStreamUtils.calculateAverage(episodeResultsList, x -> x.getTotalPayoff().get(index_i).get(index_j));
                var stdev = MathStreamUtils.calculateStdev(episodeResultsList, x -> x.getTotalPayoff().get(index_i).get(index_j));
                totalPayoffAverage.get(i).add(average);
                totalPayoffStdev.get(i).add(stdev);
            }
        }
        var averageMillisPerEpisode = MathStreamUtils.calculateAverage(episodeResultsList, (x) -> x.getDuration().toMillis());
        var stdevMillisPerEpisode = MathStreamUtils.calculateStdev(episodeResultsList, (x) -> x.getDuration().toMillis());
        return new EpisodeStatisticsBase(durations, policyCount, averagePlayerStepCount, stdevPlayerStepCount, averageMillisPerEpisode, stdevMillisPerEpisode, totalPayoffAverage, totalPayoffStdev);
    }
}
