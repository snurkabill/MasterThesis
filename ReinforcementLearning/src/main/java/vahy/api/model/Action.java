package vahy.api.model;

public interface Action {

    int getLocalIndex();

    int getCountOfAllActionsFromSameEntity();

    boolean isShadowAction();

}
