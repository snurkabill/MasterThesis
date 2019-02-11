package vahy.environment;

import vahy.api.model.Action;

import java.util.Arrays;

public enum RandomWalkAction implements Action {

    SAFE(true),
    UNSAFE(true),
    UP(false),
    DOWN(false);

    public static RandomWalkAction[] playerActions = Arrays.stream(RandomWalkAction.values()).filter(RandomWalkAction::isPlayerAction).toArray(RandomWalkAction[]::new);
    public static RandomWalkAction[] environmentActions = Arrays.stream(RandomWalkAction.values()).filter(actionType -> !actionType.isPlayerAction).toArray(RandomWalkAction[]::new);

    private final boolean isPlayerAction;

    RandomWalkAction(boolean isPlayerAction) {
        this.isPlayerAction = isPlayerAction;
    }

    @Override
    public boolean isPlayerAction() {
        return isPlayerAction;
    }

    @Override
    public int getActionIndexInPossibleActions() {
        if(this.isPlayerAction) {
            return this == SAFE ? 0 : 1;
        } else {
            return this == UP ? 0 : 1;
        }
    }
}
