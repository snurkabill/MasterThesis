package vahy.examples.bomberman;

import vahy.api.model.Action;

public enum BomberManAction implements Action {

    UP(false, 0),
    DOWN(false, 1),
    LEFT(false, 2),
    RIGHT(false, 3),
    DROP_BOMB(false, 4),

    NO_ACTION(true, 0),
    DETONATE_BOMB_IF_ANY(true, 1),
    DROP_REWARD_0(true, 2),
    DROP_REWARD_1(true, 3),
    DROP_REWARD_2(true, 4),
    DROP_REWARD_3(true, 5),
    DROP_REWARD_4(true, 6),
    DROP_REWARD_5(true, 7),
    DROP_REWARD_6(true, 8),
    DROP_REWARD_7(true, 9),
    DROP_REWARD_8(true, 10),
    DROP_REWARD_9(true, 11),
    DETONATE_BOMB_IF_ANY_DROP_REWARD_0(true, 12),
    DETONATE_BOMB_IF_ANY_DROP_REWARD_1(true, 13),
    DETONATE_BOMB_IF_ANY_DROP_REWARD_2(true, 14),
    DETONATE_BOMB_IF_ANY_DROP_REWARD_3(true, 15),
    DETONATE_BOMB_IF_ANY_DROP_REWARD_4(true, 16),
    DETONATE_BOMB_IF_ANY_DROP_REWARD_5(true, 17),
    DETONATE_BOMB_IF_ANY_DROP_REWARD_6(true, 18),
    DETONATE_BOMB_IF_ANY_DROP_REWARD_7(true, 19),
    DETONATE_BOMB_IF_ANY_DROP_REWARD_8(true, 20),
    DETONATE_BOMB_IF_ANY_DROP_REWARD_9(true, 21);



    private final boolean isEnvironmentalAction;
    private final int localIndex;

    BomberManAction(boolean isEnvironmentalAction, int localIndex) {
        this.isEnvironmentalAction = isEnvironmentalAction;
        this.localIndex = localIndex;
    }

    @Override
    public int getLocalIndex() {
        return 0;
    }

    @Override
    public int getCountOfAllActionsFromSameEntity() {
        return 0;
    }
}
