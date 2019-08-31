package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;

public interface EpisodeSetup<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    int getStepCountLimit();

    TState getInitialState();

    Policy<TAction, TPlayerObservation, TOpponentObservation, TState> getPlayerPaperPolicy();

    Policy<TAction, TPlayerObservation, TOpponentObservation, TState> getOpponentPolicy();

}
