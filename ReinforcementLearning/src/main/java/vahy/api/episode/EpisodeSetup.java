package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyRecord;

public interface EpisodeSetup<
    TAction extends Enum<TAction> & Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    TState getInitialState();

    Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getPlayerPaperPolicy();

    Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getOpponentPolicy();

    int getStepCountLimit();
}
