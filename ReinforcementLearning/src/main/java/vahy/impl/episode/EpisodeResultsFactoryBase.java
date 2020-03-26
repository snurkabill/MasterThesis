package vahy.impl.episode;

import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.EpisodeStepRecord;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.impl.model.observation.DoubleVector;

import java.time.Duration;
import java.util.List;

public class EpisodeResultsFactoryBase<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord>
    implements EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> {

    @Override
    public EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> createResults(
        List<EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> episodeHistory, int playerStepCount, int totalStepCount, double totalCumulativePayoff, Duration duration) {
        return new EpisodeResultsImpl<>(episodeHistory, playerStepCount, totalStepCount, totalCumulativePayoff, duration);
    }
}
