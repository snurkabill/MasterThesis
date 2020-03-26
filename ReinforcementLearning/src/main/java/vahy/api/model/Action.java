package vahy.api.model;

public interface Action {

    Action[] getAllPlayerActions();

    Action[] getAllOpponentActions();

    boolean isPlayerAction();

    boolean isOpponentAction();

    int getGlobalIndex();

    int getActionIndexInPlayerActions();

    int getActionIndexInOpponentActions();
}
