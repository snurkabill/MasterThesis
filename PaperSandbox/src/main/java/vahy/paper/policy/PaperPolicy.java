package vahy.paper.policy;

import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;

public interface PaperPolicy extends vahy.api.policy.Policy<ActionType, DoubleScalarReward, DoubleVectorialObservation, ImmutableStateImpl> {

    double[] getPriorActionProbabilityDistribution(ImmutableStateImpl gameState);

    DoubleScalarReward getEstimatedReward(ImmutableStateImpl gameState);

    double getEstimatedRisk(ImmutableStateImpl gameState);
}
