package vahy.environment;

import vahy.api.model.Action;

import java.util.Arrays;

public enum ActionType implements Action {

    FORWARD(true),
    TURN_RIGHT(true),
    TURN_LEFT(true),

    NOISY_RIGHT(false),
    NOISY_LEFT(false),
    TRAP(false),
    NOISY_RIGHT_TRAP(false),
    NOISY_LEFT_TRAP(false),
    NO_ACTION(false);

    public static ActionType[] playerActions = Arrays.stream(ActionType.values()).filter(ActionType::isPlayerAction).toArray(ActionType[]::new);
    public static ActionType[] environmentActions = Arrays.stream(ActionType.values()).filter(actionType -> !actionType.isPlayerAction).toArray(ActionType[]::new);
    private final boolean isPlayerAction;

    ActionType(boolean isPlayerAction) {
        this.isPlayerAction = isPlayerAction;
    }

    public boolean isPlayerAction() {
        return isPlayerAction;
    }
}
