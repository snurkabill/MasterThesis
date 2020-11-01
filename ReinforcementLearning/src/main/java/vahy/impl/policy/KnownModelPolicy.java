package vahy.impl.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecordBase;
import vahy.api.policy.RandomizedPolicy;
import vahy.api.predictor.PerfectStatePredictor;
import vahy.utils.RandomDistributionUtils;

import java.util.SplittableRandom;

public class KnownModelPolicy<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TState extends State<TAction, TObservation, TState>>
    extends RandomizedPolicy<TAction, TObservation, TState> {

    private PerfectStatePredictor<TAction, TObservation, TState> perfectPredictor;

    public KnownModelPolicy(SplittableRandom random, int policyId) {
        super(random, policyId);
    }

    @Override
    public TAction getDiscreteAction(StateWrapper<TAction, TObservation, TState> gameState) {
        if(perfectPredictor == null) {
            perfectPredictor = gameState.getKnownModelWithPerfectObservationPredictor();
            checkIfStillNull();
        }
        var actions = gameState.getAllPossibleActions();
        var probabilities = perfectPredictor.apply(gameState.getWrappedState());
        if(actions.length != probabilities.length) {
            throw new IllegalStateException("Action count differ from probability length");
        }
        return actions[RandomDistributionUtils.getRandomIndexFromDistribution(probabilities, random)];
    }

    private void checkIfStillNull() {
        if(perfectPredictor == null) {
            throw new IllegalStateException("Perfect state predictor is requested, but implementation is missing.");
        }
    }

    @Override
    public void updateStateOnPlayedAction(TAction action) {
        // this is it
    }

    @Override
    public PolicyRecordBase getPolicyRecord(StateWrapper<TAction, TObservation, TState> gameState) {
        return null;
    }
}
