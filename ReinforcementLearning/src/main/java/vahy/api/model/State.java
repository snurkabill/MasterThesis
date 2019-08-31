package vahy.api.model;

import vahy.api.model.observation.Observation;

public interface State<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    TAction[] getAllPossibleActions();

    StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState> applyAction(TAction actionType);

    TState deepCopy();

    TPlayerObservation getPlayerObservation();

    TOpponentObservation getOpponentObservation();

    String readableStringRepresentation();

    boolean isOpponentTurn();

    default boolean isPlayerTurn() {
        return !isOpponentTurn();
    }

    boolean isFinalState();

}
