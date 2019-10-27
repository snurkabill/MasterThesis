package vahy.environment;

import vahy.api.model.Action;

import java.util.Arrays;

public enum HallwayAction implements Action {

    FORWARD(true, false, 0, 0),
    TURN_RIGHT(true, false, 1, 1),
    TURN_LEFT(true, false, 2, 2),
//    RESIGN(true, false),

    NOISY_RIGHT(false, false, 3, 0),
    NOISY_LEFT(false, false, 4, 1),
    TRAP(false, true, 5, 2),
    NOISY_RIGHT_TRAP(false, true, 6, 3),
    NOISY_LEFT_TRAP(false, true, 7, 4),
    NO_ACTION(false, false, 8, 5);

    public static HallwayAction[] playerActions = Arrays.stream(HallwayAction.values()).filter(HallwayAction::isPlayerAction).toArray(HallwayAction[]::new);
    public static HallwayAction[] environmentActions = Arrays.stream(HallwayAction.values()).filter(actionType -> !actionType.isPlayerAction).toArray(HallwayAction[]::new);
    private final boolean isPlayerAction;
    private final boolean isTrap;
    private final int actionIndexInAllActions;
    private final int actionIndexInPossibleActions;

    HallwayAction(boolean isPlayerAction, boolean isTrap, int actionIndexInAllActions, int actionIndexInPossibleActions) {
        this.isPlayerAction = isPlayerAction;
        this.isTrap = isTrap;
        this.actionIndexInAllActions = actionIndexInAllActions;
        this.actionIndexInPossibleActions = actionIndexInPossibleActions;
    }

    public boolean isTrap() {
        return isTrap;
    }

    public int getActionIndexAsPlayerAction() {
        for (int i = 0; i < playerActions.length; i++) {
            if(this.equals(playerActions[i])) {
                return i;
            }
        }
        throw new IllegalStateException("Not expected state");
    }

    public int getActionIndexAsEnvironmentAction() {
        for (int i = 0; i < environmentActions.length; i++) {
            if(this.equals(environmentActions[i])) {
                return i;
            }
        }
        throw new IllegalStateException("Not expected state");
    }

    @Override
    public boolean isPlayerAction() {
        return isPlayerAction;
    }

    @Override
    public int getActionIndexInAllActions() {
        return actionIndexInAllActions;
    }

    @Override
    public int getActionIndexInPossibleActions() {
        return actionIndexInPossibleActions;
    }

    @Override
    public int getActionIndexInPlayerActions() {
        return actionIndexInPossibleActions;
    }

    @Override
    public int getActionIndexInOpponentActions() {
        return actionIndexInPossibleActions;
    }

}
