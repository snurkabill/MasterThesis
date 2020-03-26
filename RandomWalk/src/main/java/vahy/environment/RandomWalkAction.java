package vahy.environment;

import vahy.api.model.Action;

import java.util.Arrays;

public enum RandomWalkAction implements Action {

    SAFE(true, 0, 0),
    UNSAFE(true, 1, 1),
    UP(false, 0, 2),
    DOWN(false, 1, 3);

    public static RandomWalkAction[] playerActions = Arrays.stream(RandomWalkAction.values()).filter(RandomWalkAction::isPlayerAction).toArray(RandomWalkAction[]::new);
    public static RandomWalkAction[] environmentActions = Arrays.stream(RandomWalkAction.values()).filter(actionType -> !actionType.isPlayerAction).toArray(RandomWalkAction[]::new);

    private final boolean isPlayerAction;
    private final int localIndex;
    private final int globalIndex;

    RandomWalkAction(boolean isPlayerAction, int localIndex, int globalIndex) {
        this.isPlayerAction = isPlayerAction;
        this.localIndex = localIndex;
        this.globalIndex = globalIndex;
    }

    @Override
    public boolean isPlayerAction() {
        return isPlayerAction;
    }

    @Override
    public boolean isOpponentAction() {
        return !isPlayerAction;
    }

    @Override
    public int getGlobalIndex() {
        return globalIndex;
//        if(this.isPlayerAction) {
//            return this == SAFE ? 0 : 1;
//        } else {
//            return this == UP ? 0 : 1;
//        }
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
        if(!isPlayerAction) {
            return localIndex;
        } else {
            return -1;
        }
    }

    @Override
    public RandomWalkAction[] getAllPlayerActions() {
        return playerActions;
    }

    @Override
    public RandomWalkAction[] getAllOpponentActions() {
        return environmentActions;
    }
}
