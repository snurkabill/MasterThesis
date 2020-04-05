package vahy.original.environment.agent.policy.environment;

import vahy.api.policy.PolicyRecord;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.KnownModelPolicy;
import vahy.original.environment.HallwayAction;
import vahy.original.environment.state.HallwayStateImpl;

import java.util.SplittableRandom;

public class EnvironmentPolicy<TPolicyRecord extends PolicyRecord> extends KnownModelPolicy<HallwayAction, DoubleVector, HallwayStateImpl, HallwayStateImpl, TPolicyRecord> {

//    private Predictor<HallwayStateImpl> perfectPredictor;

    public EnvironmentPolicy(SplittableRandom random) {
        super(random);
    }

//    @Override
//    public double[] getActionProbabilityDistribution(HallwayStateImpl gameState) {
//        if(perfectPredictor == null) {
//            perfectPredictor = gameState.getKnownModelWithPerfectObservationPredictor();
//        }
//        return perfectPredictor.apply(gameState);
//    }
//
//    @Override
//    public HallwayAction getDiscreteAction(HallwayStateImpl gameState) {
//        if(perfectPredictor == null) {
//            perfectPredictor = gameState.getKnownModelWithPerfectObservationPredictor();
//        }
//        var actions = gameState.getAllPossibleActions();
//        var probabilities = perfectPredictor.apply(gameState);
//        if(actions.length != probabilities.length) {
//            throw new IllegalStateException("Inconsistency between enabled possible actions and predicted possible actions");
//        }
//        return actions[RandomDistributionUtils.getRandomIndexFromDistribution(probabilities, random)];
//    }
//
//    @Override
//    public void updateStateOnPlayedActions(List<HallwayAction> opponentActionList) {
//        // this is it
//    }
//
//    @Override
//    public TPolicyRecord getPolicyRecord(HallwayStateImpl gameState) {
//        return null;
//    }
}
