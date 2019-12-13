package vahy.api.model;

public interface Action<TAction extends Action<TAction>> {

    TAction[] getAllPlayerActions();

    TAction[] getAllOpponentActions();

    boolean isPlayerAction();

    int getActionIndexInPossibleActions();

}
