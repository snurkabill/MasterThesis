package vahy.impl.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.api.predictor.Predictor;
import vahy.utils.RandomDistributionUtils;

import java.util.List;
import java.util.SplittableRandom;

public class KnownModelPolicy<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> extends RandomizedPolicy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> {

    private Predictor<TState> perfectPredictor;

    protected KnownModelPolicy(SplittableRandom random) {
        super(random);
    }

    @Override
    public double[] getActionProbabilityDistribution(TState gameState) {
        if(perfectPredictor == null) {
            perfectPredictor = gameState.getKnownModelWithPerfectObservationPredictor();
        }
        return perfectPredictor.apply(gameState);
    }

    @Override
    public TAction getDiscreteAction(TState gameState) {
        if(perfectPredictor == null) {
            perfectPredictor = gameState.getKnownModelWithPerfectObservationPredictor();
        }
        var actions = gameState.getAllPossibleActions();
        var probabilities = perfectPredictor.apply(gameState);
        if(actions.length != probabilities.length) {
            perfectPredictor.apply(gameState);
            gameState.getAllPossibleActions();
            throw new IllegalStateException("Action count differ from probability length");
        }
        return actions[RandomDistributionUtils.getRandomIndexFromDistribution(probabilities, random)];
    }

    @Override
    public void updateStateOnPlayedActions(List<TAction> opponentActionList) {
        // this is it
    }

    @Override
    public TPolicyRecord getPolicyRecord(TState gameState) {
        return null;
    }
}
