package vahy.environment;

import vahy.api.model.Action;

public class HallwayAction implements Action {

    private final ActionType actionType;

    public HallwayAction(ActionType actionType) {
        this.actionType = actionType;
    }
}
