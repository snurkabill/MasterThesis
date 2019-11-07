package vahy.environment.agent.policy.environment;

import vahy.api.policy.Policy;
import vahy.api.policy.PolicyRecord;
import vahy.environment.HallwayAction;
import vahy.environment.state.EnvironmentProbabilities;
import vahy.environment.state.HallwayStateImpl;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.List;
import java.util.SplittableRandom;

public class EnvironmentPolicy<TPolicyRecord extends PolicyRecord> implements Policy<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl, TPolicyRecord> {

    private final SplittableRandom random;

    public EnvironmentPolicy(SplittableRandom random) {
        this.random = random;
    }

    @Override
    public double[] getActionProbabilityDistribution(HallwayStateImpl gameState) {
        ImmutableTuple<List<HallwayAction>, List<Double>> actions = gameState.getOpponentObservation().getProbabilities();
        return actions.getSecond().stream().mapToDouble(value -> value).toArray();
    }

    @Override
    public HallwayAction getDiscreteAction(HallwayStateImpl gameState) {
        ImmutableTuple<List<HallwayAction>, List<Double>> actions = gameState.getOpponentObservation().getProbabilities();
        return actions.getFirst().get(RandomDistributionUtils.getRandomIndexFromDistribution(actions.getSecond(), random));
    }

    @Override
    public void updateStateOnPlayedActions(List<HallwayAction> opponentActionList) {
        // this is it
    }

    @Override
    public TPolicyRecord getPolicyRecord(HallwayStateImpl gameState) {
        return null;
    }
}
