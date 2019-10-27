package vahy.api.model;

public interface Action {

    boolean isPlayerAction();

    int getActionIndexInAllActions();

    int getActionIndexInPossibleActions();

    int getActionIndexInPlayerActions();

    int getActionIndexInOpponentActions();

}
