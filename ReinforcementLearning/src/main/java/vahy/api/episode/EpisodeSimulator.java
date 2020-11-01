package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

public interface EpisodeSimulator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TState extends State<TAction, TObservation, TState>> {

    EpisodeResults<TAction, TObservation, TState> calculateEpisode(EpisodeSetup<TAction, TObservation, TState> episodeSetup);

}
