package vahy.paper.policy;

import vahy.api.model.State;
import vahy.environment.ActionType;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;

import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;

public class PaperPolicyImplWithExploration implements PaperPolicy {

    private final SplittableRandom random;
    private final PaperPolicyImpl innerPolicy;
    private double explorationConstant;
    private double temperature;

    public PaperPolicyImplWithExploration(SplittableRandom random, PaperPolicyImpl innerPolicy, double explorationConstant, double temperature) {
        this.random = random;
        this.innerPolicy = innerPolicy;
        this.explorationConstant = explorationConstant;
        this.temperature = temperature;
    }

    public double[] getPriorActionProbabilityDistribution(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        return innerPolicy.getPriorActionProbabilityDistribution(gameState);
    }

    @Override
    public DoubleScalarReward getEstimatedReward(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        return innerPolicy.getEstimatedReward(gameState);
    }

    @Override
    public double getEstimatedRisk(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        return innerPolicy.getEstimatedRisk(gameState);
    }

    public double[] getActionProbabilityDistribution(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        return innerPolicy.getActionProbabilityDistribution(gameState);
    }

    public ActionType getDiscreteAction(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        ActionType discreteAction = innerPolicy.getDiscreteAction(gameState);
        double randomDouble = random.nextDouble();
        if(randomDouble < explorationConstant) {
            double[] actionProbabilityDistribution = this.getActionProbabilityDistribution(gameState);
            double[] exponentiation = new double[actionProbabilityDistribution.length];
            for (int i = 0; i < actionProbabilityDistribution.length; i++) {
                exponentiation[i] = Math.exp(actionProbabilityDistribution[i] / temperature);
            }
            double sum = Arrays.stream(exponentiation).sum();

            for (int i = 0; i < actionProbabilityDistribution.length; i++) {
                exponentiation[i] = exponentiation[i] / sum;
            }


            ActionType[] playerActions = ActionType.playerActions;
            double rand = random.nextDouble();
            double cumulativeSum = 0.0d;

            for (int i = 0; i < actionProbabilityDistribution.length; i++) {
                cumulativeSum += exponentiation[i];
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
