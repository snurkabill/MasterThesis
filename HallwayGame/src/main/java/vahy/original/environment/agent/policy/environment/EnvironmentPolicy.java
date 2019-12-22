package vahy.original.environment.agent.policy.environment;

import vahy.api.policy.PolicyRecord;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.RandomizedPolicy;
import vahy.original.environment.HallwayAction;
import vahy.original.environment.state.EnvironmentProbabilities;
import vahy.original.environment.state.HallwayStateImpl;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.List;
import java.util.SplittableRandom;

public class EnvironmentPolicy<TPolicyRecord extends PolicyRecord> extends RandomizedPolicy<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl, TPolicyRecord> {

    public EnvironmentPolicy(SplittableRandom random) {
        super(random);
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
