package vahy.api.model;

import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;

public interface State<
    TAction extends Action,
    TReward extends Reward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> {

    TAction[] getAllPossibleActions();

    StateRewardReturn<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> applyAction(TAction actionType);

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
