package vahy.examples.bomberman;

import vahy.api.model.Action;

public enum BomberManAction implements Action {

    UP(false, 0, 5),
    DOWN(false, 1, 5),
    LEFT(false, 2, 5),
    RIGHT(false, 3, 5),
    DROP_BOMB(false, 4, 5),

    NO_ACTION(true, 0, 2),
    DETONATE_BOMB(true, 1, 2);
//    DROP_REWARD_0(true, 2, 22),
//    DROP_REWARD_1(true, 3, 22),
//    DROP_REWARD_2(true, 4, 22),
//    DROP_REWARD_3(true, 5, 22),
//    DROP_REWARD_4(true, 6, 22),
//    DROP_REWARD_5(true, 7, 22),
//    DROP_REWARD_6(true, 8, 22),
//    DROP_REWARD_7(true, 9, 22),
//    DROP_REWARD_8(true, 10, 22),
//    DROP_REWARD_9(true, 11, 22),
//    DETONATE_BOMB_IF_ANY_DROP_REWARD_0(true, 12, 22),
//    DETONATE_BOMB_IF_ANY_DROP_REWARD_1(true, 13, 22),
//    DETONATE_BOMB_IF_ANY_DROP_REWARD_2(true, 14, 22),
//    DETONATE_BOMB_IF_ANY_DROP_REWARD_3(true, 15, 22),
//    DETONATE_BOMB_IF_ANY_DROP_REWARD_4(true, 16, 22),
//    DETONATE_BOMB_IF_ANY_DROP_REWARD_5(true, 17, 22),
//    DETONATE_BOMB_IF_ANY_DROP_REWARD_6(true, 18, 22),
//    DETONATE_BOMB_IF_ANY_DROP_REWARD_7(true, 19, 22),
//    DETONATE_BOMB_IF_ANY_DROP_REWARD_8(true, 20, 22),
//    DETONATE_BOMB_IF_ANY_DROP_REWARD_9(true, 21, 22);



    private final boolean isEnvironmentalAction;
    private final int localIndex;
    private final int sameEntityActionCount;

    BomberManAction(boolean isEnvironmentalAction, int localIndex, int sameEntityActionCount) {
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
}
