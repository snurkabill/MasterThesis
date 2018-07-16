package vahy.api.model;

import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;

public interface State<TAction extends Action, TReward extends Reward, TObservation extends Observation> {

    TAction[] getAllPossibleActions();

    StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> applyAction(TAction actionType);

    State<TAction, TReward, TObservation> deepCopy();

    TObservation getObservation();

    String readableStringRepresentation();

    boolean isOpponentTurn();

    boolean isFinalState();

}
