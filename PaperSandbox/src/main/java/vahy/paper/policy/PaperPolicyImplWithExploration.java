package vahy.paper.policy;

import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.reward.DoubleReward;
import vahy.utils.RandomDistributionUtils;

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

    public double[] getPriorActionProbabilityDistribution(ImmutableStateImpl gameState) {
        return innerPolicy.getPriorActionProbabilityDistribution(gameState);
    }

    @Override
    public DoubleReward getEstimatedReward(ImmutableStateImpl gameState) {
        return innerPolicy.getEstimatedReward(gameState);
    }

    @Override
    public double getEstimatedRisk(ImmutableStateImpl gameState) {
        return innerPolicy.getEstimatedRisk(gameState);
    }

    public double[] getActionProbabilityDistribution(ImmutableStateImpl gameState) {
        return innerPolicy.getActionProbabilityDistribution(gameState);
    }

    public ActionType getDiscreteAction(ImmutableStateImpl gameState) {
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
            int index = RandomDistributionUtils.getRandomIndexFromDistribution(exponentiation, random);
            return playerActions[index];
        } else {
            return discreteAction;
        }
    }

    public void updateStateOnOpponentActions(List<ActionType> opponentActionList) {
        innerPolicy.updateStateOnOpponentActions(opponentActionList);
    }
}
