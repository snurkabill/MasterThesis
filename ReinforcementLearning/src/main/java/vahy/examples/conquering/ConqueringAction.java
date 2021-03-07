package vahy.examples.conquering;

import vahy.api.model.Action;

public enum ConqueringAction implements Action {

    FORWARD(false, 0, 2),
    WAIT(false, 1, 2),
//    RESIGN(false, 2, 3),

    KILL(true, 0, 2),
    PASS(true, 1, 2);

    private final boolean isEnvironmentalAction;
    private final int localIndex;
    private final int sameEntityActionCount;

    ConqueringAction(boolean isEnvironmentalAction, int localIndex, int sameEntityActionCount) {
        this.isEnvironmentalAction = isEnvironmentalAction;
        this.localIndex = localIndex;
        this.sameEntityActionCount = sameEntityActionCount;
    }

    public boolean isEnvironmentalAction() {
        return isEnvironmentalAction;
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
