package vahy.api.model;

public interface Action {

    Action[] getAllPlayerActions();

    Action[] getAllOpponentActions();

    boolean isPlayerAction();

    int getActionIndexInPossibleActions();

}
