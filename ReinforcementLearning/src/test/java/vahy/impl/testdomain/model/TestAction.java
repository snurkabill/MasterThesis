package vahy.impl.testdomain.model;

import vahy.api.model.Action;

import java.util.Arrays;
import java.util.Comparator;

public enum TestAction implements Action {

    A(true, 'A', 1, 0, 0),
    B(true, 'B', 2, 1, 1),
    C(true, 'C', 3, 2, 2),

    X(false, 'X', -1, 3, 0),
    Y(false, 'Y', -2, 4, 1),
    Z(false, 'Z', -3, 5, 2);

    public static TestAction[] playerActions = Arrays.stream(TestAction.values()).filter(TestAction::isPlayerAction).sorted(Comparator.comparing(TestAction::getActionIndexInPlayerActions)).toArray(TestAction[]::new);
    public static TestAction[] opponentActions = Arrays.stream(TestAction.values()).filter(TestAction::isOpponentAction).sorted(Comparator.comparing(TestAction::getActionIndexInOpponentActions)).toArray(TestAction[]::new);
    private final boolean isPlayerAction;
    private final boolean isOpponentAction;
    private final char charRepresentation;
    private final double reward;
    private final int globalIndex;
    private final int localIndex;

    TestAction(boolean isPlayerAction, char charRepresentation, double reward, int globalIndex, int localIndex) {
        this.isPlayerAction = isPlayerAction;
        this.isOpponentAction = !isPlayerAction;
        this.charRepresentation = charRepresentation;
        this.reward = reward;
        this.globalIndex = globalIndex;
        this.localIndex = localIndex;
    }

    public char getCharRepresentation() {
        return charRepresentation;
    }

    @Override
    public TestAction[] getAllPlayerActions() {
        return playerActions;
    }

    @Override
    public TestAction[] getAllOpponentActions() {
        return opponentActions;
    }

    public boolean isPlayerAction() {
        return isPlayerAction;
    }

    @Override
    public boolean isOpponentAction() {
        return isOpponentAction;
    }

    @Override
    public int getGlobalIndex() {
        return globalIndex;
    }

    @Override
    public int getActionIndexInPlayerActions() {
        if(isPlayerAction) {
            return localIndex;
        } else {
            return -1;
        }

    }

    @Override
    public int getActionIndexInOpponentActions() {
        if(isOpponentAction) {
            return localIndex;
        } else {
            return -1;
        }
    }

    public double getReward() {
        return reward;
    }

}
