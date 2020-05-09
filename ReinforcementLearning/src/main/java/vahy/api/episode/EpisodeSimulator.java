package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;

public interface EpisodeSimulator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    EpisodeResults<TAction, TObservation, TState, TPolicyRecord> calculateEpisode(EpisodeSetup<TAction, TObservation, TState, TPolicyRecord> episodeSetup);

}
