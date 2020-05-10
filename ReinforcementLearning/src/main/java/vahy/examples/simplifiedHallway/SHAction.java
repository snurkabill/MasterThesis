package vahy.examples.simplifiedHallway;

import vahy.api.model.Action;

import java.util.Arrays;
import java.util.Comparator;

public enum SHAction implements Action {

    UP(true, 0, 0),
    DOWN(true, 1, 1),
    RIGHT(true, 2, 2),
    LEFT(true, 3, 3),
//    RESIGN(true, false),

    TRAP(false, 0, 4),
    NO_ACTION(false, 1, 5);

    public static SHAction[] playerActions = Arrays
        .stream(SHAction.values())
        .filter(x -> x.isPlayerAction(SHState.PLAYER_ID))
        .sorted(Comparator.comparing(shAction -> shAction.getActionIndexInPlayerActions(SHState.PLAYER_ID)))
        .toArray(SHAction[]::new);

    private final boolean isPlayerAction;
    private final int localIndex;
    private final int globalIndex;

    SHAction(boolean isPlayerAction, int localIndex, int globalIndex) {
        this.isPlayerAction = isPlayerAction;
        this.localIndex = localIndex;
        this.globalIndex = globalIndex;
    }

    @Override
    public boolean isPlayerAction(int playerId) {
        if(playerId == SHState.PLAYER_ID) {
            return isPlayerAction;
        } if(playerId == SHState.ENVIRONMENT_ID) {
            return !isPlayerAction;
        } else {
            throw new IllegalStateException("Not expected playerId: [" + playerId + "] at this scenario");
        }
    }

    @Override
    public boolean isOpponentAction(int playerId) {
        if(playerId == SHState.PLAYER_ID) {
            return !isPlayerAction;
        } else if(playerId == SHState.ENVIRONMENT_ID) {
            return isPlayerAction;
        } else {
            throw new IllegalStateException("Not expected playerId: [" + playerId + "] at this scenario");
        }
    }

    @Override
    public int getGlobalIndex() {
        return globalIndex;
    }

    @Override
    public int getActionIndexInPlayerActions(int playerId) {
        if(isPlayerAction && playerId == SHState.PLAYER_ID) {
            return localIndex;
        }
        return -1;
    }

    @Override
    public int getActionIndexInOpponentActions(int playerId) {
        if(!isPlayerAction && playerId == SHState.PLAYER_ID) {
            return localIndex;
        }
        return -1;
    }

//    @Override
//    public boolean isPlayerAction(int playerId) {
//        return this.isPlayerAction;
//    }
//
//    @Override
//    public boolean isOpponentAction(int playerId) {
//        return !this.isPlayerAction;
//    }
//
//    @Override
//    public int getGlobalIndex() {
//        return this.globalIndex;
//    }
//
//    @Override
//    public int getActionIndexInPlayerActions() {
//        if(isPlayerAction) {
//            return localIndex;
//        }
//        return -1;
//    }
//
//    @Override
//    public int getActionIndexInOpponentActions() {
//        if(!isPlayerAction) {
//            return localIndex;
//        }
//        return -1;
//    }
}
