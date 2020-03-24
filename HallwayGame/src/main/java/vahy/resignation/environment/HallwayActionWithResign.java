package vahy.resignation.environment;

import vahy.api.model.Action;

import java.util.Arrays;
import java.util.Comparator;

public enum HallwayActionWithResign implements Action {

    FORWARD(true, false, 0, 0),
    TURN_RIGHT(true, false, 1, 1),
    TURN_LEFT(true, false, 2, 2),
    RESIGN(true, false, 3, 3),

    NOISY_RIGHT(false, false, 0, 4),
    NOISY_LEFT(false, false, 1, 5),
    TRAP(false, true, 2, 6),
    NOISY_RIGHT_TRAP(false, true, 3, 7),
    NOISY_LEFT_TRAP(false, true, 4, 8),
    NO_ACTION(false, false, 5, 9);

    public static HallwayActionWithResign[] playerActions = Arrays.stream(HallwayActionWithResign.values()).filter(HallwayActionWithResign::isPlayerAction).sorted(Comparator.comparing(HallwayActionWithResign::getActionIndexInPlayerActions)).toArray(HallwayActionWithResign[]::new);
    public static HallwayActionWithResign[] environmentActions = Arrays.stream(HallwayActionWithResign.values()).filter(HallwayActionWithResign::isOpponentAction).sorted(Comparator.comparing(HallwayActionWithResign::getActionIndexInPlayerActions)).toArray(HallwayActionWithResign[]::new);
    private final boolean isPlayerAction;
    private final int localIndex;
    private final int globalIndex;

    HallwayActionWithResign(boolean isPlayerAction, boolean isTrap, int localIndex, int globalIndex) {
        this.isPlayerAction = isPlayerAction;
        this.localIndex = localIndex;
        this.globalIndex = globalIndex;
    }

    @Override
    public HallwayActionWithResign[] getAllPlayerActions() {
        return playerActions;
    }

    @Override
    public HallwayActionWithResign[] getAllOpponentActions() {
        return environmentActions;
    }

    @Override
    public boolean isPlayerAction() {
        return isPlayerAction;
    }

    @Override
    public boolean isOpponentAction() {
        return !isPlayerAction;
    }

    @Override
    public int getGlobalIndex() {
        return globalIndex;
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
