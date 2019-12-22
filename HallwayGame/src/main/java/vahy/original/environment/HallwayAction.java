package vahy.original.environment;

import vahy.api.model.Action;

import java.util.Arrays;
import java.util.Comparator;

public enum HallwayAction implements Action<HallwayAction> {

    FORWARD(true, false, 0, 0),
    TURN_RIGHT(true, false, 1, 1),
    TURN_LEFT(true, false, 2, 2),
//    RESIGN(true, false),

    NOISY_RIGHT(false, false, 0, 3),
    NOISY_LEFT(false, false, 1, 4),
    TRAP(false, true, 2, 5),
    NOISY_RIGHT_TRAP(false, true, 3, 6),
    NOISY_LEFT_TRAP(false, true, 4, 7),
    NO_ACTION(false, false, 5, 8);

    public static HallwayAction[] playerActions = Arrays.stream(HallwayAction.values()).filter(HallwayAction::isPlayerAction).sorted(Comparator.comparing(HallwayAction::getActionIndexInPlayerActions)).toArray(HallwayAction[]::new);
    public static HallwayAction[] environmentActions = Arrays.stream(HallwayAction.values()).filter(HallwayAction::isOpponentAction).sorted(Comparator.comparing(HallwayAction::getActionIndexInPlayerActions)).toArray(HallwayAction[]::new);
    private final boolean isPlayerAction;
    private final boolean isTrap;
    private final int localIndex;
    private final int globalIndex;

    HallwayAction(boolean isPlayerAction, boolean isTrap, int localIndex, int globalIndex) {
        this.isPlayerAction = isPlayerAction;
        this.isTrap = isTrap;
        this.localIndex = localIndex;
        this.globalIndex = globalIndex;
    }

    public boolean isTrap() {
        return isTrap;
    }

    @Override
    public HallwayAction[] getAllPlayerActions() {
        return playerActions;
    }

    @Override
    public HallwayAction[] getAllOpponentActions() {
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
