package vahy.impl.testdomain.emptySpace;

import vahy.api.model.Action;

public enum EmptySpaceAction implements Action {
    A(true, 0, 2),
    B(true,  1, 2),
    AA(false,  0, 2),
    BB(false,  1, 2);

    public static EmptySpaceAction[] playerActions = new EmptySpaceAction[] {A, B};
    public static EmptySpaceAction[] opponentActions = new EmptySpaceAction[] {AA, BB};
    private final boolean isPlayerAction;
    private final int localIndex;
    private final int sameEntityActionCount;

    EmptySpaceAction(boolean isPlayerAction, int localIndex, int sameEntityActionCount) {
        this.isPlayerAction = isPlayerAction;
        this.localIndex = localIndex;
        this.sameEntityActionCount = sameEntityActionCount;
    }

    @Override
    public int getLocalIndex() {
        return localIndex;
    }

    @Override
    public int getCountOfAllActionsFromSameEntity() {
        return sameEntityActionCount;
    }
}