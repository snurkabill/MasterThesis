package vahy.environment;

import vahy.api.model.Action;

import java.util.Arrays;
import java.util.Comparator;

public enum MarketAction implements Action<MarketAction> {

    NO_ACTION(true, 0, 0),
    OPEN_LONG(true, 1, 1),
    OPEN_SHORT(true, 2, 2),
    REVERSE(true, 3, 3),
    CLOSE(true, 4, 4),

    UP(false, 0, 5),
    DOWN(false, 1, 6);

    public static MarketAction[] playerActions = Arrays.stream(MarketAction.values()).filter(MarketAction::isPlayerAction).sorted(Comparator.comparing(MarketAction::getActionIndexInPlayerActions)).toArray(MarketAction[]::new);
    public static MarketAction[] environmentActions = Arrays.stream(MarketAction.values()).filter(MarketAction::isOpponentAction).sorted(Comparator.comparing(MarketAction::getActionIndexInOpponentActions)).toArray(MarketAction[]::new);

    public static MarketAction[] noPositionPlayerActions = new MarketAction[] {NO_ACTION, OPEN_LONG, OPEN_SHORT};
    public static MarketAction[] openPositionPlayerActions = new MarketAction[] {NO_ACTION, REVERSE, CLOSE};

    private final boolean isPlayerAction;
    private final int localIndex;
    private final int globalIndex;

    MarketAction(boolean isPlayerAction, int localIndex, int globalIndex) {
        this.isPlayerAction = isPlayerAction;
        this.localIndex = localIndex;
        this.globalIndex = globalIndex;
    }

    @Override
    public MarketAction[] getAllPlayerActions() {
        return playerActions;
    }

    @Override
    public MarketAction[] getAllOpponentActions() {
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
