package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

import java.time.Duration;
import java.util.List;

public interface EpisodeResultsFactory<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TState extends State<TAction, TObservation, TState>> {

    EpisodeResults<TAction, TObservation, TState> createResults(
        int episodeId,
        List<EpisodeStepRecord<TAction, TObservation, TState>> episodeHistory,
        PolicyIdTranslationMap policyIdTranslationMap,
        int policyCount,
        List<Integer> playerStepCountList,
        List<Double> averageDurationPerDecision,
        int totalStepCount,
        List<Double> totalCumulativePayoffList,
        Duration duration
    );
}
