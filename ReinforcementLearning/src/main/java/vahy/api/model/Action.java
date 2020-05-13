package vahy.api.model;

public interface Action {

//    boolean isPlayerAction(int playerId);

//    boolean isOpponentAction(int playerId, int opponentId);

    int getLocalIndex();

    int getCountOfAllActionsFromSameEntity();

}
