package vahy.domain;

import vahy.api.model.Action;

import java.util.Arrays;
import java.util.Comparator;

public enum SHAction2 implements Action {

    UP(true, 0, 0),
    DOWN(true, 1, 1),
    RIGHT(true, 2, 2),
    LEFT(true, 3, 3),
//    RESIGN(true, false),

    TRAP(false, 0, 4),
    NO_ACTION(false, 1, 5);

    public static SHAction2[] playerActions = Arrays.stream(SHAction2.values()).filter(SHAction2::isPlayerAction).sorted(Comparator.comparing(SHAction2::getActionIndexInPlayerActions)).toArray(SHAction2[]::new);
    public static SHAction2[] environmentActions = Arrays.stream(SHAction2.values()).filter(SHAction2::isOpponentAction).sorted(Comparator.comparing(SHAction2::getActionIndexInPlayerActions)).toArray(SHAction2[]::new);

    private final boolean isPlayerAction;
    private final int localIndex;
    private final int globalIndex;

    SHAction2(boolean isPlayerAction, int localIndex, int globalIndex) {
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
