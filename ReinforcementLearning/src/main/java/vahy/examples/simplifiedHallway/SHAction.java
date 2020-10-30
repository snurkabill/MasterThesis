package vahy.examples.simplifiedHallway;

import vahy.api.model.Action;

public enum SHAction implements Action {

    UP(0, 4),
    DOWN(1, 4),
    RIGHT(2, 4),
    LEFT(3, 4),

    TRAP(0, 2),
    NO_ACTION(1, 2);

    private final int localIndex;
    private final int sameEntityActionCount;

    SHAction(int localIndex, int sameEntityActionCount) {
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

    @Override
    public boolean isShadowAction() {
        return false;
    }
}
