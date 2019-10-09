package vahy.paperGenerics.reinforcement.episode;

import vahy.api.episode.EpisodeResults;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.PaperPolicyRecord;

public interface PaperEpisodeResults<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PaperPolicyRecord>
    extends EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> {

    boolean isRiskHit();

}
