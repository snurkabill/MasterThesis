package vahy.environment.agent.policy;

import vahy.api.model.State;
import vahy.environment.ActionType;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.model.DoubleVectorialObservation;

public interface IOneHotPolicy {

    ActionType getDiscreteAction(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState);
}
