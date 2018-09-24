package vahy.AlphaGo.policy;

import vahy.api.model.State;
import vahy.api.policy.Policy;
import vahy.environment.ActionType;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.policy.random.UniformRandomWalkPolicy;

import java.util.List;
import java.util.SplittableRandom;

public class AlphaGoPolicyImplWithExploration implements AlphaGoPolicy {

    private final SplittableRandom random;
    private final AlphaGoPolicyImpl innerPolicy;
    private final Policy<ActionType, DoubleScalarReward, DoubleVectorialObservation> randomPolicy;
    private double explorationConstant;

    public AlphaGoPolicyImplWithExploration(SplittableRandom random, AlphaGoPolicyImpl innerPolicy, double explorationConstant) {
        this.random = random;
        this.randomPolicy = new UniformRandomWalkPolicy<>(random);
        this.innerPolicy = innerPolicy;
        this.explorationConstant = explorationConstant;
    }

    public double[] getPriorActionProbabilityDistribution(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        return innerPolicy.getPriorActionProbabilityDistribution(gameState);
    }

    @Override
    public DoubleScalarReward getEstimatedReward(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        return innerPolicy.getEstimatedReward(gameState);
    }

    public double[] getActionProbabilityDistribution(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        return innerPolicy.getActionProbabilityDistribution(gameState);
    }

    public ActionType getDiscreteAction(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        ActionType discreteAction = innerPolicy.getDiscreteAction(gameState);
        if(random.nextDouble() < explorationConstant) {
            double[] actionProbabilityDistribution = this.getActionProbabilityDistribution(gameState);
            ActionType[] playerActions = ActionType.playerActions;
            double rand = random.nextDouble();
            double cumulativeSum = 0.0d;

            for (int i = 0; i < actionProbabilityDistribution.length; i++) {
                cumulativeSum += actionProbabilityDistribution[i];
                if(rand < cumulativeSum) {
                    return playerActions[i];
                }
            }
            throw new IllegalStateException("Numerically unstable probability calculation");
        } else {
            return discreteAction;
        }
    }

    public void updateStateOnOpponentActions(List<ActionType> opponentActionList) {
        innerPolicy.updateStateOnOpponentActions(opponentActionList);
    }
}
