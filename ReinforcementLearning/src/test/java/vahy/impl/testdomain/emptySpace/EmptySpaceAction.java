package vahy.impl.testdomain.emptySpace;

import vahy.api.model.Action;

public enum EmptySpaceAction implements Action {
    A(0, 2),
    B(1, 2),
    AA(0, 2),
    BB(1, 2);

    public static EmptySpaceAction[] playerActions = new EmptySpaceAction[] {A, B};
    public static EmptySpaceAction[] opponentActions = new EmptySpaceAction[] {AA, BB};
    private final int localIndex;
    private final int sameEntityActionCount;

    EmptySpaceAction(int localIndex, int sameEntityActionCount) {
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