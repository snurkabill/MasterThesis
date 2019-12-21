package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;

public interface EpisodeSimulator<
    TAction extends Enum<TAction> & Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> calculateEpisode(EpisodeSetup<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeSetup);

}
