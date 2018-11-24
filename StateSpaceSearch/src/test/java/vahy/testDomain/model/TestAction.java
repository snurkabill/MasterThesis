package vahy.testDomain.model;

import vahy.api.model.Action;

import java.util.Arrays;

public enum TestAction implements Action {

    A(true, 'A', 1),
    B(true, 'B', 2),
    C(true, 'C', 3),

    X(false, 'X', -1),
    Y(false, 'Y', -2),
    Z(false, 'Z', -3);

    public static TestAction[] playerActions = Arrays.stream(TestAction.values()).filter(TestAction::isPlayerAction).toArray(TestAction[]::new);
    public static TestAction[] opponentActions = Arrays.stream(TestAction.values()).filter(actionType -> !actionType.isPlayerAction).toArray(TestAction[]::new);
    private final boolean isPlayerAction;
    private final char charRepresentation;
    private final double reward;

    TestAction(boolean isPlayerAction, char charRepresentation, double reward) {
        this.isPlayerAction = isPlayerAction;
        this.charRepresentation = charRepresentation;
        this.reward = reward;
    }

    public char getCharRepresentation() {
        return charRepresentation;
    }

    public boolean isPlayerAction() {
        return isPlayerAction;
    }

    public double getReward() {
        return reward;
    }
}
