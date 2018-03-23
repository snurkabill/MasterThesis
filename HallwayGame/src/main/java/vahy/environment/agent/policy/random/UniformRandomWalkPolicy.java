package vahy.environment.agent.policy.random;

import vahy.environment.ActionType;
import vahy.environment.state.IState;
import vahy.utils.EnumUtils;

import java.util.Arrays;
import java.util.Random;

public class UniformRandomWalkPolicy extends AbstarctRandomWalkPolicy {

    public UniformRandomWalkPolicy(Random random) {
        super(random);
    }

    @Override
    public ActionType getDiscreteAction(IState gameState) {
        // ignoring impossible actions here
        return EnumUtils.generateRandomEnumUniformly(ActionType.class, getRandom());
    }

    @Override
    public double[] getActionProbabilityDistribution(IState gameState) {
        // ignoring impossible actions here
        double[] probabilities = new double[ActionType.values().length];
        Arrays.fill(probabilities, 1.0 / (double) probabilities.length);
        return probabilities;
    }
}
