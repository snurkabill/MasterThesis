package vahy.environment.agent.policy;

import vahy.api.model.State;
import vahy.environment.ActionType;

public interface IOneHotPolicy {

    ActionType getDiscreteAction(State gameState);
}
