package vahy.environment.agent.policy.environment;

import vahy.api.policy.Policy;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleReward;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.List;
import java.util.SplittableRandom;

public class EnvironmentPolicy implements Policy<ActionType, DoubleReward, DoubleVectorialObservation, ImmutableStateImpl> {

    private final SplittableRandom random;

    public EnvironmentPolicy(SplittableRandom random) {
        this.random = random;
    }

    @Override
    public double[] getActionProbabilityDistribution(ImmutableStateImpl gameState) {
        ImmutableTuple<List<ActionType>, List<Double>> actions = gameState.environmentActionsWithProbabilities();
        return actions.getSecond().stream().mapToDouble(value -> value).toArray();
    }

    @Override
    public ActionType getDiscreteAction(ImmutableStateImpl gameState) {
        ImmutableTuple<List<ActionType>, List<Double>> actions = gameState.environmentActionsWithProbabilities();
        return actions.getFirst().get(RandomDistributionUtils.getRandomIndexFromDistribution(actions.getSecond(), random));
    }

    @Override
    public void updateStateOnOpponentActions(List<ActionType> opponentActionList) {
        // this is it
    }
}
