package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

import java.util.List;

public interface GameSampler<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState>> sampleEpisodes(int episodeBatchSize);
}
