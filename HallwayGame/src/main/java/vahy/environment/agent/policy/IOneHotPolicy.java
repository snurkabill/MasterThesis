package vahy.environment.agent.policy;

import vahy.environment.ActionType;
import vahy.environment.state.IState;

public interface IOneHotPolicy {

    ActionType getDiscreteAction(IState gameState);
}
