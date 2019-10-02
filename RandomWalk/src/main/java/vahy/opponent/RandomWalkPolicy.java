package vahy.opponent;

import vahy.environment.RandomWalkAction;
import vahy.environment.RandomWalkProbabilities;
import vahy.environment.RandomWalkState;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.policy.PaperPolicy;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.List;
import java.util.SplittableRandom;

public class RandomWalkPolicy implements PaperPolicy<RandomWalkAction, DoubleVector, RandomWalkProbabilities, RandomWalkState> {

    private final SplittableRandom random;

    public RandomWalkPolicy(SplittableRandom random) {
        this.random = random;
    }

    @Override
    public double[] getPriorActionProbabilityDistribution(RandomWalkState gameState) {
        return new double[0];
    }

    @Override
    public double getEstimatedReward(RandomWalkState gameState) {
        return 0.0;
    }

    @Override
    public double getEstimatedRisk(RandomWalkState gameState) {
        return 0;
    }

    @Override
    public double getInnerRiskAllowed() {
        return 0;
    }

    @Override
    public double[] getActionProbabilityDistribution(RandomWalkState gameState) {
        ImmutableTuple<List<RandomWalkAction>, List<Double>> actions = gameState.getOpponentObservation().getProbabilities();
        return actions.getSecond().stream().mapToDouble(value -> value).toArray();
    }

    @Override
    public RandomWalkAction getDiscreteAction(RandomWalkState gameState) {
        ImmutableTuple<List<RandomWalkAction>, List<Double>> actions = gameState.getOpponentObservation().getProbabilities();
        return actions.getFirst().get(RandomDistributionUtils.getRandomIndexFromDistribution(actions.getSecond(), random));
    }

    @Override
    public void updateStateOnPlayedActions(List<RandomWalkAction> opponentActionList) {
        // This is it
    }
}
