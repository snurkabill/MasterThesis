package vahy.environment.agent.policy;

import vahy.environment.ActionType;

public interface IStatefulPolicy extends IPolicy {

    void updateState(ActionType applyAction);

}
