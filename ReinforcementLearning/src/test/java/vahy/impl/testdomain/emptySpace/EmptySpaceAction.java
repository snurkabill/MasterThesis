package vahy.impl.testdomain.emptySpace;

import vahy.api.model.Action;

import java.util.Arrays;
import java.util.Comparator;

public enum EmptySpaceAction implements Action {
    A(true, false, 0, 0),
    B(true, false, 1, 1),
//    C(true, false, 2, 2),
//    D(true, false, 3, 3),
//    E(true, false, 4, 4),
    AA(false, true, 2, 0),
    BB(false, true, 3, 1);
//    CC(false, true, 7, 2),
//    DD(false, true, 8, 3),
//    EE(false, true, 9, 4);


    public static EmptySpaceAction[] playerActions = Arrays.stream(EmptySpaceAction.values()).filter(EmptySpaceAction::isPlayerAction).sorted(Comparator.comparing(EmptySpaceAction::getActionIndexInPlayerActions)).toArray(EmptySpaceAction[]::new);
    public static EmptySpaceAction[] opponentActions = Arrays.stream(EmptySpaceAction.values()).filter(EmptySpaceAction::isOpponentAction).sorted(Comparator.comparing(EmptySpaceAction::getActionIndexInOpponentActions)).toArray(EmptySpaceAction[]::new);
    private final boolean isPlayerAction;
    private final boolean isOpponentAction;
    private final int globalIndex;
    private final int localIndex;

    EmptySpaceAction(boolean isPlayerAction, boolean isOpponentAction, int globalIndex, int localIndex) {
        this.isPlayerAction = isPlayerAction;
        this.isOpponentAction = isOpponentAction;
        this.globalIndex = globalIndex;
        this.localIndex = localIndex;
    }

    @Override
    public Action[] getAllPlayerActions() {
        return playerActions;
    }

    @Override
    public Action[] getAllOpponentActions() {
        return opponentActions;
    }

    @Override
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
}