package vahy.environment.agent.policy;

import vahy.api.model.State;
import vahy.environment.ActionType;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.model.DoubleVectorialObservation;

public interface IPolicy {

    double[] getActionProbabilityDistribution(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState);

}
