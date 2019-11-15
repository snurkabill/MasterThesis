package vahy.api.model;

public interface Action<TAction extends Action> {

    TAction[] getAllPlayerActions();

    TAction[] getAllOpponentActions();

    boolean isPlayerAction();

    int getActionIndexInPossibleActions();

}
