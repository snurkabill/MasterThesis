package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecord;

import java.util.List;
import java.util.SplittableRandom;

public interface GameSampler<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    List<EpisodeResults<TAction, TObservation, TState, TPolicyRecord>> sampleEpisodes(int episodeBatchSize, int stepCountLimit, PolicyMode policyMode);
}
