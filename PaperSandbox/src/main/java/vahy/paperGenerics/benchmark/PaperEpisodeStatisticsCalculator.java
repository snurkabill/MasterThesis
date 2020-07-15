package vahy.paperGenerics.benchmark;

import vahy.api.benchmark.EpisodeStatisticsCalculator;
import vahy.api.episode.EpisodeResults;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.benchmark.EpisodeStatisticsBase;
import vahy.impl.benchmark.EpisodeStatisticsCalculatorBase;
import vahy.paperGenerics.PaperState;
import vahy.utils.MathStreamUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class PaperEpisodeStatisticsCalculator<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends PaperState<TAction, TObservation, TState>>
    implements EpisodeStatisticsCalculator<TAction, TObservation, TState, PaperEpisodeStatistics> {

    private final EpisodeStatisticsCalculatorBase<TAction, TObservation, TState> baseCalculator = new EpisodeStatisticsCalculatorBase<>();

    @Override
    public PaperEpisodeStatistics calculateStatistics(List<EpisodeResults<TAction, TObservation, TState>> episodeResultsList, Duration duration) {
        EpisodeStatisticsBase base = baseCalculator.calculateStatistics(episodeResultsList, duration);

        var policyCount = episodeResultsList.get(0).getPolicyCount();
        List<Long> riskHitCounter = new ArrayList<>(policyCount);
        List<Double> riskHitRatio = new ArrayList<>(policyCount);
        List<Double> riskHitStdev = new ArrayList<>(policyCount);
        for (int i = 0; i < policyCount; i++) {
            var index = i;
            var riskHitCount = episodeResultsList.stream().filter(x -> x.getFinalState().isRiskHit(index)).count();
            riskHitCounter.add(riskHitCount);
            riskHitRatio.add(riskHitCount / (double) episodeResultsList.size());
            riskHitStdev.add(MathStreamUtils.calculateStdev(episodeResultsList, value -> value.getFinalState().isRiskHit(index) ? 1.0 : 0.0));
        }
        return new PaperEpisodeStatistics(base, riskHitCounter, riskHitRatio, riskHitStdev);
    }
}
