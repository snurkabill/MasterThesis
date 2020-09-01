package vahy.examples.patrolling;

import vahy.api.model.Action;

public enum PatrollingAction implements Action {

    AAA;



    @Override
    public int getLocalIndex() {
        return 0;
    }

    @Override
    public int getCountOfAllActionsFromSameEntity() {
        return 0;
    }
}
