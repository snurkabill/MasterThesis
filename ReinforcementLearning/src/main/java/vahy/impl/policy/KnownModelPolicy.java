package vahy.impl.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.api.predictor.Predictor;
import vahy.impl.RoundBuilder;
import vahy.utils.RandomDistributionUtils;

import java.util.List;
import java.util.SplittableRandom;

public class KnownModelPolicy<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>,
    TPolicyRecord extends PolicyRecord>
    extends RandomizedPolicy<TAction, TObservation, TState, TPolicyRecord> {

    private Predictor<TState> perfectPredictor;

    public KnownModelPolicy(SplittableRandom random, int policyId) {
        super(random, policyId);
    }

    @Override
    public double[] getActionProbabilityDistribution(StateWrapper<TAction, TObservation, TState> gameState) {
        if(perfectPredictor == null) {
            perfectPredictor = gameState.getKnownModelWithPerfectObservationPredictor();
            checkIfStillNull();
        }
        return perfectPredictor.apply(gameState.getWrappedState());
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
    public void updateStateOnPlayedActions(List<TAction> opponentActionList) {
        // this is it
    }

    @Override
    public TPolicyRecord getPolicyRecord(StateWrapper<TAction, TObservation, TState> gameState) {
        return null;
    }
}
