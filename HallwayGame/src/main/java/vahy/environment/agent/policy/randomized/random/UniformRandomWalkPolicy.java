package vahy.environment.agent.policy.randomized.random;

import vahy.api.model.State;
import vahy.environment.ActionType;
import vahy.environment.agent.policy.IStatefulPolicy;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.model.DoubleVectorialObservation;

import java.util.Arrays;
import java.util.SplittableRandom;

public class UniformRandomWalkPolicy extends AbstractRandomWalkPolicy implements IStatefulPolicy {

    public UniformRandomWalkPolicy(SplittableRandom random) {
        super(random);
    }

    @Override
    public ActionType getDiscreteAction(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        ActionType[] actions = gameState.getAllPossibleActions();
        return actions[getRandom().nextInt(actions.length)];
    }

    @Override
    public double[] getActionProbabilityDistribution(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        // ignoring impossible actions here
        double[] probabilities = new double[ActionType.values().length];
        Arrays.fill(probabilities, 1.0 / (double) probabilities.length);
        return probabilities;
    }

    @Override
    public void updateState(ActionType applyAction) {
        // this is it
        // implements IStatefulPolicy just because we want to plug such policy into episodic runs for comparison
    }
}
