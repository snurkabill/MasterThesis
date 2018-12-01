package vahy.paper.policy;

import vahy.environment.HallwayAction;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;

public interface PaperPolicy extends vahy.api.policy.Policy<HallwayAction, DoubleReward, DoubleVector, ImmutableStateImpl> {

    double[] getPriorActionProbabilityDistribution(ImmutableStateImpl gameState);

    DoubleReward getEstimatedReward(ImmutableStateImpl gameState);

    double getEstimatedRisk(ImmutableStateImpl gameState);
}
