package vahy.environment.agent.policy.environment;

import vahy.api.model.State;
import vahy.environment.ActionType;
import vahy.environment.agent.policy.IOneHotPolicy;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.model.DoubleVectorialObservation;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.List;
import java.util.SplittableRandom;

public class EnvironmentPolicy implements IOneHotPolicy {

    private final SplittableRandom random;

    public EnvironmentPolicy(SplittableRandom random) {
        this.random = random;
    }

    @Override
    public ActionType getDiscreteAction(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        ImmutableStateImpl castedGameState = (ImmutableStateImpl) gameState;
        ImmutableTuple<List<ActionType>, List<Double>> actions = castedGameState.environmentActionsWithProbabilities();
        return actions.getFirst().get(RandomDistributionUtils.getRandomIndexFromDistribution(actions.getSecond(), random));
    }
}