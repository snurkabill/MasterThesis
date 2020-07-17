package vahy.impl.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecordBase;
import vahy.api.policy.RandomizedPolicy;

import java.util.SplittableRandom;

public class UniformRandomWalkPolicy<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>>
    extends RandomizedPolicy<TAction, TObservation, TState> {

    private TAction action;

    public UniformRandomWalkPolicy(SplittableRandom random, int policyId) {
        super(random, policyId);
    }

    @Override
    public TAction getDiscreteAction(StateWrapper<TAction, TObservation, TState> gameState) {
        TAction[] actions = gameState.getAllPossibleActions();
        return actions[random.nextInt(actions.length)];
    }

    @Override
    public void updateStateOnPlayedAction(TAction opponentAction) {
        // this is it
    }

    @Override
    public PolicyRecordBase getPolicyRecord(StateWrapper<TAction, TObservation, TState> gameState) {
        if(action == null) {
            action = gameState.getAllPossibleActions()[0];
        }
        return new PolicyRecordBase(EMPTY_ARRAY, 0.0);
    }
}
