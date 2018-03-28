package vahy.api.model;

public interface State<TAction extends Action, TReward extends Reward, TObservation extends Observation> {

    TAction[] getAllPossibleActions();

    StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> applyAction(TAction actionType);

    State<TAction, TReward, TObservation> deepCopy();

    TObservation getObservation();

    boolean isFinalState();

}
