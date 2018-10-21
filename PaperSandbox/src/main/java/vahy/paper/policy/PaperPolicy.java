package vahy.paper.policy;

import vahy.api.model.State;
import vahy.environment.ActionType;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;

public interface PaperPolicy extends vahy.api.policy.Policy<ActionType, DoubleScalarReward, DoubleVectorialObservation> {

    double[] getPriorActionProbabilityDistribution(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState);

    DoubleScalarReward getEstimatedReward(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState);

    double getEstimatedRisk(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState);
}
