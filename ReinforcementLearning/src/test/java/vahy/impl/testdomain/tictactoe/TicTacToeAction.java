package vahy.impl.testdomain.tictactoe;

import vahy.api.model.Action;

import java.util.Arrays;
import java.util.Comparator;

public enum TicTacToeAction implements Action {

    _0x0(true, true,0, 0, 0, 0),
    _0x1(true, true,0, 1, 1, 1),
    _0x2(true, true,0, 2, 2, 2),
    _1x0(true, true,1, 0, 3, 3),
    _1x1(true, true,1, 1, 4, 4),
    _1x2(true, true,1, 2, 5, 5),
    _2x0(true, true,2, 0, 6, 6),
    _2x1(true, true,2, 1, 7, 7),
    _2x2(true, true,2, 2, 8, 8);

    public static TicTacToeAction[] playerActions = Arrays.stream(TicTacToeAction.values()).filter(TicTacToeAction::isPlayerAction).sorted(Comparator.comparing(TicTacToeAction::getActionIndexInPlayerActions)).toArray(TicTacToeAction[]::new);
    public static TicTacToeAction[] environmentActions = Arrays.stream(TicTacToeAction.values()).filter(TicTacToeAction::isOpponentAction).sorted(Comparator.comparing(TicTacToeAction::getActionIndexInOpponentActions)).toArray(TicTacToeAction[]::new);
    private final boolean isPlayerAction;
    private final boolean isOpponentAction;
    private final int x;
    private final int y;
    private final int globalIndex;
    private final int localIndex;

    TicTacToeAction(boolean isPlayerAction, boolean isOpponentAction, int x, int y, int localIndex, int globalIndex) {
        this.isPlayerAction = isPlayerAction;
        this.isOpponentAction = isOpponentAction;
        this.x = x;
        this.y = y;
        this.globalIndex = globalIndex;
        this.localIndex = localIndex;
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
    public boolean isOpponentAction() {
        return isOpponentAction;
    }

    @Override
    public int getGlobalIndex() {
        return globalIndex;
    }

    @Override
    public int getActionIndexInPlayerActions() {
        return globalIndex;
    }

    @Override
    public int getActionIndexInOpponentActions() {
        return globalIndex;
    }
}
