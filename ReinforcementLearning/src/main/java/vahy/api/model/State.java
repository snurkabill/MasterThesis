package vahy.api.model;

import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;

public interface State<TAction extends Action, TReward extends Reward, TObservation extends Observation, TState extends State<TAction, TReward, TObservation, TState>> {

    TAction[] getAllPossibleActions();

    StateRewardReturn<TAction, TReward, TObservation, TState> applyAction(TAction actionType);

    TState deepCopy();

    TObservation getObservation();

    String readableStringRepresentation();

    boolean isOpponentTurn();

    default boolean isPlayerTurn() {
        return !isOpponentTurn();
    }

    boolean isFinalState();

}
