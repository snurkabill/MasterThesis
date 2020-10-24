package vahy.examples.bomberman;

import vahy.api.model.Action;

public enum BomberManAction implements Action {

    UP(false, false, 0, 5),
    DOWN(false, false, 1, 5),
    LEFT(false, false, 2, 5),
    RIGHT(false, false, 3, 5),
    DROP_BOMB(false, false, 4, 5),

    NO_ACTION(true, false, 0, 2),
    DETONATE_BOMB(true, false, 1, 2),

    NO_ACTION_REWARD(false, true, 0, 2),
    RESPAWN_REWARD(false, true, 1, 2);



    private final boolean isEnvironmentalAction;
    private final boolean isRewardAction;
    private final int localIndex;
    private final int sameEntityActionCount;

    BomberManAction(boolean isEnvironmentalAction, boolean isRewardAction, int localIndex, int sameEntityActionCount) {
        this.isEnvironmentalAction = isEnvironmentalAction;
        this.isRewardAction = isRewardAction;
        this.localIndex = localIndex;
        this.sameEntityActionCount = sameEntityActionCount;
    }

    public boolean isEnvironmentalAction() {
        return isEnvironmentalAction;
    }

    public boolean isGoldAction() {
        return isRewardAction;
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
