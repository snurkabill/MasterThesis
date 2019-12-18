package vahy.impl.testdomain.tictactoe;

import vahy.api.model.Action;

import java.util.Arrays;

public enum TicTacToeAction implements Action<TicTacToeAction> {

    _0x0_player(true, 0, 0),
    _0x1_player(true, 0, 1),
    _0x2_player(true, 0, 2),
    _1x0_player(true, 1, 0),
    _1x1_player(true, 1, 1),
    _1x2_player(true, 1, 2),
    _2x0_player(true, 2, 0),
    _2x1_player(true, 2, 1),
    _2x2_player(true, 2, 2),
    _0x0_opponent(false, 0, 0),
    _0x1_opponent(false, 0, 1),
    _0x2_opponent(false, 0, 2),
    _1x0_opponent(false, 1, 0),
    _1x1_opponent(false, 1, 1),
    _1x2_opponent(false, 1, 2),
    _2x0_opponent(false, 2, 0),
    _2x1_opponent(false, 2, 1),
    _2x2_opponent(false, 2, 2);

    public static TicTacToeAction[] playerActions = Arrays.stream(TicTacToeAction.values()).filter(TicTacToeAction::isPlayerAction).toArray(TicTacToeAction[]::new);
    public static TicTacToeAction[] environmentActions = Arrays.stream(TicTacToeAction.values()).filter(actionType -> !actionType.isPlayerAction).toArray(TicTacToeAction[]::new);
    private final boolean isPlayerAction;
    private final int x;
    private final int y;

    TicTacToeAction(boolean isPlayerAction, int x, int y) {
        this.isPlayerAction = isPlayerAction;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public TicTacToeAction[] getAllPlayerActions() {
        return playerActions;
    }

    @Override
    public TicTacToeAction[] getAllOpponentActions() {
        return environmentActions;
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
