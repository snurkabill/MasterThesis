package vahy.impl.testdomain.emptySpace;

import vahy.api.model.Action;

public enum EmptySpaceAction implements Action {
    A(true, false, 0, 0),
    B(true, false, 1, 1),
    AA(false, true, 2, 0),
    BB(false, true, 3, 1);

    public static EmptySpaceAction[] playerActions = new EmptySpaceAction[] {A, B};
    public static EmptySpaceAction[] opponentActions = new EmptySpaceAction[] {AA, BB};
    private final boolean isPlayerAction;
    private final boolean isOpponentAction;
    private final int globalIndex;
    private final int localIndex;

    EmptySpaceAction(boolean isPlayerAction, boolean isOpponentAction, int globalIndex, int localIndex) {
        this.isPlayerAction = isPlayerAction;
        this.isOpponentAction = isOpponentAction;
        this.globalIndex = globalIndex;
        this.localIndex = localIndex;
    }

    @Override
    public boolean isPlayerAction(int playerId) {
        if(isOpponentAction) {
            return playerId == 0;
        } else {
            return playerId != 0;
        }
    }

    @Override
    public boolean isOpponentAction(int playerId) {
        if(isOpponentAction) {
            return playerId != 0;
        } else {
            return playerId == 0;
        }
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