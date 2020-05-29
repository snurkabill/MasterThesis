package vahy.impl.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecordBase;

import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;

public class UniformRandomWalkPolicy<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>>
    extends RandomizedPolicy<TAction, TObservation, TState, PolicyRecordBase> {

    public UniformRandomWalkPolicy(SplittableRandom random, int policyId) {
        super(random, policyId);
    }

    @Override
    public double[] getActionProbabilityDistribution(StateWrapper<TAction, TObservation, TState> gameState) {
        double[] probabilities = new double[gameState.getAllPossibleActions().length];
        Arrays.fill(probabilities, 1.0 / (double) probabilities.length);
        return probabilities;
    }

    @Override
    public TAction getDiscreteAction(StateWrapper<TAction, TObservation, TState> gameState) {
        TAction[] actions = gameState.getAllPossibleActions();
        return actions[random.nextInt(actions.length)];
    }

    @Override
    public void updateStateOnPlayedActions(List<TAction> opponentActionList) {
        // this is it
    }

    @Override
    public PolicyRecordBase getPolicyRecord(StateWrapper<TAction, TObservation, TState> gameState) {
        return new PolicyRecordBase(getActionProbabilityDistribution(gameState), 0.0);
    }
}
