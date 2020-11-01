package vahy.impl.episode;

import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.EpisodeStepRecord;
import vahy.api.episode.PolicyIdTranslationMap;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

import java.time.Duration;
import java.util.List;

public class EpisodeResultsFactoryBase<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TState extends State<TAction, TObservation, TState>>
    implements EpisodeResultsFactory<TAction, TObservation, TState> {

    @Override
    public EpisodeResults<TAction, TObservation, TState> createResults(List<EpisodeStepRecord<TAction, TObservation, TState>> episodeHistory,
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
