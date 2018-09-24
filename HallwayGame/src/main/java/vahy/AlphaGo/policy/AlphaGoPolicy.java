package vahy.AlphaGo.policy;

import vahy.api.model.State;
import vahy.api.policy.Policy;
import vahy.environment.ActionType;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;

public interface AlphaGoPolicy extends Policy<ActionType, DoubleScalarReward, DoubleVectorialObservation> {

    double[] getPriorActionProbabilityDistribution(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState);

    DoubleScalarReward getEstimatedReward(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState);
}
