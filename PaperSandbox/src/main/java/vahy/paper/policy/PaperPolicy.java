package vahy.paper.policy;

import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleReward;

public interface PaperPolicy extends vahy.api.policy.Policy<ActionType, DoubleReward, DoubleVectorialObservation, ImmutableStateImpl> {

    double[] getPriorActionProbabilityDistribution(ImmutableStateImpl gameState);

    DoubleReward getEstimatedReward(ImmutableStateImpl gameState);

    double getEstimatedRisk(ImmutableStateImpl gameState);
}
