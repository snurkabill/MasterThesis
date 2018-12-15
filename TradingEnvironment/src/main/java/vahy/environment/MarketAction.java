package vahy.environment;

import vahy.api.model.Action;

import java.util.Arrays;

public enum MarketAction implements Action {

    NO_ACTION(true),
    OPEN_LONG(true),
    OPEN_SHORT(true),
    REVERSE(true),
    CLOSE(true),

    UP(false),
    DOWN(false);

    public static MarketAction[] playerActions = Arrays.stream(MarketAction.values()).filter(MarketAction::isPlayerAction).toArray(MarketAction[]::new);
    public static MarketAction[] environmentActions = Arrays.stream(MarketAction.values()).filter(actionType -> !actionType.isPlayerAction).toArray(MarketAction[]::new);
    private final boolean isPlayerAction;

    MarketAction(boolean isPlayerAction) {
        this.isPlayerAction = isPlayerAction;
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
        throw new IllegalStateException("Not expected state");
    }
}
