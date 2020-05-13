package vahy.examples.simplifiedHallway;

import vahy.api.model.Action;

public enum SHAction implements Action {

    UP(true, 0, 4),
    DOWN(true, 1, 4),
    RIGHT(true, 2, 4),
    LEFT(true, 3, 4),
//    RESIGN(true, false),

    TRAP(false, 0, 2),
    NO_ACTION(false, 1, 2);

//    public static SHAction[] playerActions = Arrays
//        .stream(SHAction.values())
//        .filter(x -> x.isPlayerAction(SHState.PLAYER_ID))
//        .sorted(Comparator.comparing(shAction -> shAction.getActionIndexInPlayerActions(SHState.PLAYER_ID)))
//        .toArray(SHAction[]::new);

    private final boolean isPlayerAction;
    private final int localIndex;
    private final int sameEntityActionCount;

    SHAction(boolean isPlayerAction, int localIndex, int sameEntityActionCount) {
        this.isPlayerAction = isPlayerAction;
        this.localIndex = localIndex;
        this.sameEntityActionCount = sameEntityActionCount;
    }

//    @Override
//    public boolean isPlayerAction(int playerId) {
//        if(playerId == SHState.PLAYER_ID) {
//            return isPlayerAction;
//        } if(playerId == SHState.ENVIRONMENT_ID) {
//            return !isPlayerAction;
//        } else {
//            throw new IllegalStateException("Not expected playerId: [" + playerId + "] at this scenario");
//        }
//    }

    @Override
    public int getLocalIndex() {
        return localIndex;
    }

    public int getCountOfAllActionsFromSameEntity() {
        return sameEntityActionCount;
    }

//    @Override
//    public boolean isOpponentAction(int playerId) {
//        if(playerId == SHState.PLAYER_ID) {
//            return !isPlayerAction;
//        } else if(playerId == SHState.ENVIRONMENT_ID) {
//            return isPlayerAction;
//        } else {
//            throw new IllegalStateException("Not expected playerId: [" + playerId + "] at this scenario");
//        }
//    }
//
//    @Override
//    public int getGlobalIndex() {
//        return globalIndex;
//    }
//
//    @Override
//    public int getActionIndexInPlayerActions(int playerId) {
//        if(isPlayerAction && playerId == SHState.PLAYER_ID) {
//            return localIndex;
//        }
//        return -1;
//    }
//
//    @Override
//    public int getActionIndexInOpponentActions(int playerId) {
//        if(!isPlayerAction && playerId == SHState.PLAYER_ID) {
//            return localIndex;
//        }
//        return -1;
//    }

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
