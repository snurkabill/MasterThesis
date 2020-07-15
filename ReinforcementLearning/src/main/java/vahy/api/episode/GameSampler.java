package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyMode;

import java.util.List;

public interface GameSampler<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>> {

    List<EpisodeResults<TAction, TObservation, TState>> sampleEpisodes(int episodeBatchSize, int stepCountLimit, PolicyMode policyMode);
}
