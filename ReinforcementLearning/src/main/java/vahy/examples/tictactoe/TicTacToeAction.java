package vahy.examples.tictactoe;

import vahy.api.model.Action;

public enum TicTacToeAction implements Action {

    _0x0(0, 0, 0, 9),
    _0x1(0, 1, 1, 9),
    _0x2(0, 2, 2, 9),
    _1x0(1, 0, 3, 9),
    _1x1(1, 1, 4, 9),
    _1x2(1, 2, 5, 9),
    _2x0(2, 0, 6, 9),
    _2x1(2, 1, 7, 9),
    _2x2(2, 2, 8, 9);

    private final int x;
    private final int y;
    private final int sameEntityActionCount;
    private final int localIndex;

    TicTacToeAction(int x, int y, int localIndex, int sameEntityActionCount) {
        this.x = x;
        this.y = y;
        this.sameEntityActionCount = sameEntityActionCount;
        this.localIndex = localIndex;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public int getLocalIndex() {
        return localIndex;
    }

    @Override
    public int getCountOfAllActionsFromSameEntity() {
        return sameEntityActionCount;
    }

//    @Override
//    public boolean isPlayerAction(int playerId) {
//        return true;
//    }
//
//    @Override
//    public boolean isOpponentAction(int playerId) {
//        return true;
//    }
//
//    @Override
//    public int getGlobalIndex() {
//        return globalIndex;
//    }
//
//    @Override
//    public int getActionIndexInPlayerActions(int playerId) {
//        return localIndex;
//    }
//
//    @Override
//    public int getActionIndexInOpponentActions(int playerId) {
//        return localIndex;
//    }

}
