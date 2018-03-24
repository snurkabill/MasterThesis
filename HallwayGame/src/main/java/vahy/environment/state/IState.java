package vahy.environment.state;

import vahy.environment.ActionType;

public interface IState {

    ActionType[] getListOfPossibleActions();

    RewardStateReturn applyAction(ActionType actionType);

    IState deepCopy();

    double[] getFeatureVector();

    boolean isFinalState();
}
