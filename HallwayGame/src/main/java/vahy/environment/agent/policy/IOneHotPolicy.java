package vahy.environment.agent.policy;

import vahy.environment.ActionType;

public interface IOneHotPolicy {

    ActionType getDiscreteAction(IState gameState);
}
