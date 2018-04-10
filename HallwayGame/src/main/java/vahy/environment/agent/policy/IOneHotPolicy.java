package vahy.environment.agent.policy;

import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;

public interface IOneHotPolicy {

    ActionType getDiscreteAction(ImmutableStateImpl gameState);
}
