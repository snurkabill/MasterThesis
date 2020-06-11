package vahy.impl.episode;

import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.EpisodeStepRecord;
import vahy.api.episode.PolicyIdTranslationMap;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.policy.PolicyRecord;
import vahy.impl.model.observation.DoubleVector;

import java.time.Duration;
import java.util.List;

public class EpisodeResultsFactoryBase<
    TAction extends Enum<TAction> & Action,
    TObservation extends DoubleVector,
    TState extends State<TAction, TObservation, TState>,
    TPolicyRecord extends PolicyRecord>
    implements EpisodeResultsFactory<TAction, TObservation, TState, TPolicyRecord> {

    @Override
    public EpisodeResults<TAction, TObservation, TState, TPolicyRecord> createResults(List<EpisodeStepRecord<TAction, TObservation, TState, TPolicyRecord>> episodeHistory,
                                                                                      PolicyIdTranslationMap policyIdTranslationMap,
                                                                                      int policyCount,
                                                                                      List<Integer> playerStepCount,
                                                                                      List<Double> averageDurationPerDecision,
                                                                                      int totalStepCountList,
                                                                                      List<Double> totalCumulativePayoff,
                                                                                      Duration duration)
    {
        return new EpisodeResultsImpl<>(episodeHistory, policyIdTranslationMap, policyCount, playerStepCount, averageDurationPerDecision, totalStepCountList, totalCumulativePayoff, duration);
    }
}
