package vahy.api.model;

public interface Action {

    boolean isPlayerAction(int playerId);

    boolean isOpponentAction(int playerId);

    int getGlobalIndex();

    int getActionIndexInPlayerActions(int playerId);

    int getActionIndexInOpponentActions(int playerId);
}
