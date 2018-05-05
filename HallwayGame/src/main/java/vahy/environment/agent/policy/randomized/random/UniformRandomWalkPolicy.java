package vahy.environment.agent.policy.randomized.random;

import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;

import java.util.Arrays;
import java.util.SplittableRandom;

public class UniformRandomWalkPolicy extends AbstractRandomWalkPolicy {

    public UniformRandomWalkPolicy(SplittableRandom random) {
        super(random);
    }

    @Override
    public ActionType getDiscreteAction(ImmutableStateImpl gameState) {
        ActionType[] actions = gameState.getAllPossibleActions();
        return actions[getRandom().nextInt(actions.length)];
    }

    @Override
    public double[] getActionProbabilityDistribution(ImmutableStateImpl gameState) {
        // ignoring impossible actions here
        double[] probabilities = new double[ActionType.values().length];
        Arrays.fill(probabilities, 1.0 / (double) probabilities.length);
        return probabilities;
    }
}
