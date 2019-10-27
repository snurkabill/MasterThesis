package vahy.testDomain.model;

import vahy.api.model.Action;

import java.util.Arrays;

public enum TestAction implements Action {

    A(true, 'A', 1, 0, 0),
    B(true, 'B', 2, 1, 1),
    C(true, 'C', 3, 2, 2),

    X(false, 'X', -1, 3, 0),
    Y(false, 'Y', -2, 4, 1),
    Z(false, 'Z', -3, 5, 2);

    public static TestAction[] playerActions = Arrays.stream(TestAction.values()).filter(TestAction::isPlayerAction).toArray(TestAction[]::new);
    public static TestAction[] opponentActions = Arrays.stream(TestAction.values()).filter(actionType -> !actionType.isPlayerAction).toArray(TestAction[]::new);
    private final boolean isPlayerAction;
    private final char charRepresentation;
    private final double reward;
    private final int actionIndexInAllActions;
    private final int actionIndexInPossibleActions;

    TestAction(boolean isPlayerAction, char charRepresentation, double reward, int actionIndexInAllActions, int actionIndexInPossibleActions) {
        this.isPlayerAction = isPlayerAction;
        this.charRepresentation = charRepresentation;
        this.reward = reward;
        this.actionIndexInAllActions = actionIndexInAllActions;
        this.actionIndexInPossibleActions = actionIndexInPossibleActions;
    }

    public char getCharRepresentation() {
        return charRepresentation;
    }

    public boolean isPlayerAction() {
        return isPlayerAction;
    }

    @Override
    public int getActionIndexInAllActions() {
        return actionIndexInAllActions;
    }

    @Override
    public int getActionIndexInPossibleActions() {
        return actionIndexInPossibleActions;
    }

    @Override
    public int getActionIndexInPlayerActions() {
        return actionIndexInPossibleActions;
    }

    @Override
    public int getActionIndexInOpponentActions() {
        return actionIndexInPossibleActions;
    }

    public double getReward() {
        return reward;
    }

}
