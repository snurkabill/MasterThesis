package vahy.environment.agent.policy.environment;

import vahy.api.policy.Policy;
import vahy.environment.HallwayAction;
import vahy.environment.state.HallwayStateImpl;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.List;
import java.util.SplittableRandom;

public class EnvironmentPolicy implements Policy<HallwayAction, DoubleReward, DoubleVector, HallwayStateImpl> {

    private final SplittableRandom random;

    public EnvironmentPolicy(SplittableRandom random) {
        this.random = random;
    }

    @Override
    public double[] getActionProbabilityDistribution(HallwayStateImpl gameState) {
        ImmutableTuple<List<HallwayAction>, List<Double>> actions = gameState.environmentActionsWithProbabilities();
        return actions.getSecond().stream().mapToDouble(value -> value).toArray();
    }

    @Override
    public HallwayAction getDiscreteAction(HallwayStateImpl gameState) {
        ImmutableTuple<List<HallwayAction>, List<Double>> actions = gameState.environmentActionsWithProbabilities();
        return actions.getFirst().get(RandomDistributionUtils.getRandomIndexFromDistribution(actions.getSecond(), random));
    }

    @Override
    public void updateStateOnOpponentActions(List<HallwayAction> opponentActionList) {
        // this is it
    }
}
