package vahy.paperGenerics.reinforcement.episode;

import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.EpisodeStepRecord;
import vahy.api.model.Action;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.PaperPolicyRecord;

import java.time.Duration;
import java.util.List;

public class PaperEpisodeResultsFactory<
    TAction extends Enum<TAction> & Action,
    TObservation extends DoubleVector,
    TState extends PaperState<TAction, TObservation, TState>,
    TPolicyRecord extends PaperPolicyRecord>
    implements EpisodeResultsFactory<TAction, TObservation, TState, TPolicyRecord> {

    @Override
    public EpisodeResults<TAction, TObservation, TState, TPolicyRecord> createResults(
        List<EpisodeStepRecord<TAction, TObservation, TState, TPolicyRecord>> episodeHistory, int playerStepCount, int totalStepCount, double totalCumulativePayoff, Duration duration) {
        return new PaperEpisodeResults<>(episodeHistory, playerStepCount, totalStepCount, totalCumulativePayoff, duration);
    }
}
