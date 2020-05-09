package vahy.api.model.observation;

public interface ActionWrapper {
    
    boolean isPlayerAction();

    boolean isOpponentAction();

    int getGlobalIndex();

    int getActionIndexInPlayerActions();

    int getActionIndexInOpponentActions();

    int getPlayerIdWrapper();

}
