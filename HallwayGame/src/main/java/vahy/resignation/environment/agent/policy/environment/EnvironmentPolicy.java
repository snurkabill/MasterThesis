package vahy.resignation.environment.agent.policy.environment;

import vahy.api.policy.PolicyRecord;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.RandomizedPolicy;
import vahy.resignation.environment.HallwayActionWithResign;
import vahy.resignation.environment.state.EnvironmentProbabilities;
import vahy.resignation.environment.state.HallwayStateWithResign;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.List;
import java.util.SplittableRandom;

public class EnvironmentPolicy<TPolicyRecord extends PolicyRecord> extends RandomizedPolicy<HallwayActionWithResign, DoubleVector, EnvironmentProbabilities, HallwayStateWithResign, TPolicyRecord> {

    public EnvironmentPolicy(SplittableRandom random) {
        super(random);
    }

    @Override
    public double[] getActionProbabilityDistribution(HallwayStateWithResign gameState) {
        ImmutableTuple<List<HallwayActionWithResign>, List<Double>> actions = gameState.getOpponentObservation().getProbabilities();
        return actions.getSecond().stream().mapToDouble(value -> value).toArray();
    }

    @Override
    public HallwayActionWithResign getDiscreteAction(HallwayStateWithResign gameState) {
        ImmutableTuple<List<HallwayActionWithResign>, List<Double>> actions = gameState.getOpponentObservation().getProbabilities();
        return actions.getFirst().get(RandomDistributionUtils.getRandomIndexFromDistribution(actions.getSecond(), random));
    }

    @Override
    public void updateStateOnPlayedActions(List<HallwayActionWithResign> opponentActionList) {
        // this is it
    }

    @Override
    public TPolicyRecord getPolicyRecord(HallwayStateWithResign gameState) {
        return null;
    }
}
