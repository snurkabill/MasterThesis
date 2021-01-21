package vahy.benchmark;

import vahy.RiskPolicyRecord;
import vahy.RiskState;
import vahy.api.benchmark.EpisodeStatisticsCalculator;
import vahy.api.episode.EpisodeResults;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.benchmark.EpisodeStatisticsBase;
import vahy.impl.benchmark.EpisodeStatisticsCalculatorBase;
import vahy.utils.MathStreamUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RiskEpisodeStatisticsCalculator<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TState extends RiskState<TAction, TObservation, TState>>
    implements EpisodeStatisticsCalculator<TAction, TObservation, TState, RiskEpisodeStatistics> {

    private final EpisodeStatisticsCalculatorBase<TAction, TObservation, TState> baseCalculator = new EpisodeStatisticsCalculatorBase<>();

    @Override
    public RiskEpisodeStatistics calculateStatistics(List<EpisodeResults<TAction, TObservation, TState>> episodeResultsList, Duration duration) {
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

        var riskExhaustedIndexAverage = new ArrayList<Double>(policyCount);
        var riskExhaustedIndexStdev = new ArrayList<Double>(policyCount);
        var riskThresholdAtEndAverage = new ArrayList<Double>(policyCount);
        var riskThresholdAtEndStdev = new ArrayList<Double>(policyCount);

        for (int policyIndex = 0; policyIndex < policyCount; policyIndex++) {
            var indexAggregator = new ArrayList<Double>();
            var thresholdAggregator = new ArrayList<Double>();

            for (EpisodeResults<TAction, TObservation, TState> episodeResult : episodeResultsList) {
                var translationMap = episodeResult.getPolicyIdTranslationMap();
                int inGameEntityIndex = translationMap.getInGameEntityId(policyIndex);

                var policyStates = episodeResult.getEpisodeHistory()
                    .stream()
                    .filter(x -> x.getFromState().isInGame(inGameEntityIndex) && x.getFromState().getInGameEntityIdOnTurn() == inGameEntityIndex)
                    .collect(Collectors.toList());

                var latestState = policyStates.get(policyStates.size() - 1);
                var policyRecord = latestState.getPolicyStepRecord();

                if(policyRecord != null && policyRecord.getClass() == RiskPolicyRecord.class) {
                    var castedPolicyRecord = (RiskPolicyRecord) policyRecord;
                    var totalRiskAllowed = castedPolicyRecord.getTotalRiskAllowed();
                    thresholdAggregator.add(totalRiskAllowed);

                    var bestIndex = 0;
                    for (int i = 0; i < policyStates.size(); i++) {
                        var castedRecord = ((RiskPolicyRecord) policyStates.get(i).getPolicyStepRecord());
                        bestIndex = i;
                        if(castedRecord.getTotalRiskAllowed() >= 1.0) {
                            break;
                        }
                    }
                    indexAggregator.add((double) bestIndex);
                } else {
                    thresholdAggregator.add(1.0);
                    indexAggregator.add(0.0);
                }
            }

            var indexAverage = MathStreamUtils.calculateAverage(indexAggregator);
            riskExhaustedIndexAverage.add(indexAverage);
            riskExhaustedIndexStdev.add(MathStreamUtils.calculateStdev(indexAggregator, indexAverage));

            var thresholdAverage = MathStreamUtils.calculateAverage(thresholdAggregator);
            riskThresholdAtEndAverage.add(thresholdAverage);
            riskThresholdAtEndStdev.add(MathStreamUtils.calculateStdev(thresholdAggregator, thresholdAverage));
        }

        return new RiskEpisodeStatistics(base, riskHitCounter, riskHitRatio, riskHitStdev, riskExhaustedIndexAverage, riskExhaustedIndexStdev, riskThresholdAtEndAverage, riskThresholdAtEndStdev);
    }
}
