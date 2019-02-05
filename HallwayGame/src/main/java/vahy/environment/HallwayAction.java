package vahy.environment;

import vahy.api.model.Action;

import java.util.Arrays;

public enum HallwayAction implements Action {

    FORWARD(true, false),
    TURN_RIGHT(true, false),
    TURN_LEFT(true, false),
//    RESIGN(true, false),

    NOISY_RIGHT(false, false),
    NOISY_LEFT(false, false),
    TRAP(false, true),
    NOISY_RIGHT_TRAP(false, true),
    NOISY_LEFT_TRAP(false, true),
    NO_ACTION(false, false);

    public static HallwayAction[] playerActions = Arrays.stream(HallwayAction.values()).filter(HallwayAction::isPlayerAction).toArray(HallwayAction[]::new);
    public static HallwayAction[] environmentActions = Arrays.stream(HallwayAction.values()).filter(actionType -> !actionType.isPlayerAction).toArray(HallwayAction[]::new);
    private final boolean isPlayerAction;
    private final boolean isTrap;

    HallwayAction(boolean isPlayerAction, boolean isTrap) {
        this.isPlayerAction = isPlayerAction;
        this.isTrap = isTrap;
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
    public int getActionIndexInPossibleActions() {
        if(this.isPlayerAction) {
            for (int i = 0; i < playerActions.length; i++) {
                if(this.equals(playerActions[i])) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < environmentActions.length; i++) {
                if(this.equals(environmentActions[i])) {
                    return i;
                }
            }
        }
        throw new IllegalStateException("Not expected state. Called on action: [" + this.toString() + "]");
    }

}
