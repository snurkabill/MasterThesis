package vahy.paper.policy;

import vahy.environment.HallwayAction;
import vahy.environment.state.HallwayStateImpl;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;

public interface PaperPolicy extends vahy.api.policy.Policy<HallwayAction, DoubleReward, DoubleVector, HallwayStateImpl> {

    double[] getPriorActionProbabilityDistribution(HallwayStateImpl gameState);

    DoubleReward getEstimatedReward(HallwayStateImpl gameState);

    double getEstimatedRisk(HallwayStateImpl gameState);
}
