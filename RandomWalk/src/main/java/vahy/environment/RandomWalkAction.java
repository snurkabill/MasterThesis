package vahy.environment;

import vahy.api.model.Action;

import java.util.Arrays;

public enum RandomWalkAction implements Action {

    SAFE(true, 0, 0),
    UNSAFE(true, 1, 1),
    UP(false, 2, 0),
    DOWN(false, 3, 1);

    public static RandomWalkAction[] playerActions = Arrays.stream(RandomWalkAction.values()).filter(RandomWalkAction::isPlayerAction).toArray(RandomWalkAction[]::new);
    public static RandomWalkAction[] environmentActions = Arrays.stream(RandomWalkAction.values()).filter(actionType -> !actionType.isPlayerAction).toArray(RandomWalkAction[]::new);

    private final boolean isPlayerAction;
    private final int actionIndexInAllActions;
    private final int actionIndexInPossibleActions;

    RandomWalkAction(boolean isPlayerAction, int actionIndexInAllActions, int actionIndexInPossibleActions) {
        this.isPlayerAction = isPlayerAction;
        this.actionIndexInAllActions = actionIndexInAllActions;
        this.actionIndexInPossibleActions = actionIndexInPossibleActions;
    }

    @Override
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
}
