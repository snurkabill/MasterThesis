package vahy.impl.testdomain.tictactoe;

import vahy.api.model.Action;

public enum TicTacToeAction implements Action {

    _0x0(0, 0, 0, 0),
    _0x1(0, 1, 1, 1),
    _0x2(0, 2, 2, 2),
    _1x0(1, 0, 3, 3),
    _1x1(1, 1, 4, 4),
    _1x2(1, 2, 5, 5),
    _2x0(2, 0, 6, 6),
    _2x1(2, 1, 7, 7),
    _2x2(2, 2, 8, 8);

    private final int x;
    private final int y;
    private final int globalIndex;
    private final int localIndex;

    TicTacToeAction(int x, int y, int localIndex, int globalIndex) {
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
    public boolean isPlayerAction(int playerId) {
        return true;
    }

    @Override
    public boolean isOpponentAction(int playerId) {
        return true;
    }

    @Override
    public int getGlobalIndex() {
        return globalIndex;
    }

    @Override
    public int getActionIndexInPlayerActions(int playerId) {
        return localIndex;
    }

    @Override
    public int getActionIndexInOpponentActions(int playerId) {
        return localIndex;
    }

}
