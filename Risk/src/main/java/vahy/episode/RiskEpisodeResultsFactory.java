package vahy.episode;

import vahy.RiskState;
import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.EpisodeStepRecord;
import vahy.api.episode.PolicyIdTranslationMap;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.episode.EpisodeResultsFactoryBase;

import java.time.Duration;
import java.util.List;

public class RiskEpisodeResultsFactory<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TState extends RiskState<TAction, TObservation, TState>>
    implements EpisodeResultsFactory<TAction, TObservation, TState> {

    private final EpisodeResultsFactoryBase<TAction, TObservation, TState> baseFactory = new EpisodeResultsFactoryBase<>();

    @Override
    public EpisodeResults<TAction, TObservation, TState> createResults(int episodeId,
                                                                       List<EpisodeStepRecord<TAction, TObservation, TState>> episodeHistory,
                                                                       PolicyIdTranslationMap policyIdTranslationMap,
                                                                       int policyCount,
                                                                       List<Integer> playerStepCountList,
                                                                       List<Double> averageDurationPerDecision,
                                                                       int totalStepCount,
                                                                       List<Double> totalCumulativePayoffList,
                                                                       Duration duration) {

        var base = baseFactory.createResults(episodeId, episodeHistory, policyIdTranslationMap, policyCount, playerStepCountList, averageDurationPerDecision, totalStepCount, totalCumulativePayoffList, duration);
        return new RiskEpisodeResults<>(base);
    }
}
