package vahy.domain;

import vahy.api.model.Action;

import java.util.Arrays;
import java.util.Comparator;

public enum SHAction implements Action {

    UP(true, 0, 0),
    DOWN(true, 1, 1),
    RIGHT(true, 2, 2),
    LEFT(true, 3, 3),
//    RESIGN(true, false),

    TRAP(false, 0, 4),
    NO_ACTION(false, 1, 5);

    public static SHAction[] playerActions = Arrays.stream(SHAction.values()).filter(SHAction::isPlayerAction).sorted(Comparator.comparing(SHAction::getActionIndexInPlayerActions)).toArray(SHAction[]::new);
    public static SHAction[] environmentActions = Arrays.stream(SHAction.values()).filter(SHAction::isOpponentAction).sorted(Comparator.comparing(SHAction::getActionIndexInPlayerActions)).toArray(SHAction[]::new);

    private final boolean isPlayerAction;
    private final int localIndex;
    private final int globalIndex;

    SHAction(boolean isPlayerAction, int localIndex, int globalIndex) {
        this.isPlayerAction = isPlayerAction;
        this.localIndex = localIndex;
        this.globalIndex = globalIndex;
    }

    @Override
    public Action[] getAllPlayerActions() {
        return playerActions;
    }

    @Override
    public Action[] getAllOpponentActions() {
        return environmentActions;
    }

    @Override
    public boolean isPlayerAction() {
        return this.isPlayerAction;
    }

    @Override
    public boolean isOpponentAction() {
        return !this.isPlayerAction;
    }

    @Override
    public int getGlobalIndex() {
        return this.globalIndex;
    }

    @Override
    public int getActionIndexInPlayerActions() {
        if(isPlayerAction) {
            return localIndex;
        }
        return -1;
    }

    @Override
    public int getActionIndexInOpponentActions() {
        if(!isPlayerAction) {
            return localIndex;
        }
        return -1;
    }
}
