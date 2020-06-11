package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;

import java.time.Duration;
import java.util.List;

public interface EpisodeResultsFactory<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    EpisodeResults<TAction, TObservation, TState, TPolicyRecord> createResults(
        List<EpisodeStepRecord<TAction, TObservation, TState, TPolicyRecord>> episodeHistory,
        PolicyIdTranslationMap policyIdTranslationMap,
        int policyCount,
        List<Integer> playerStepCountList,
        List<Double> averageDurationPerDecision,
        int totalStepCount,
        List<Double> totalCumulativePayoffList,
        Duration duration
    );
}
