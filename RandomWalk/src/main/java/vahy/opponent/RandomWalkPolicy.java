package vahy.opponent;

import vahy.environment.RandomWalkAction;
import vahy.environment.RandomWalkState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.KnownModelPolicy;
import vahy.paperGenerics.policy.PaperPolicyRecord;

import java.util.SplittableRandom;

public class RandomWalkPolicy extends KnownModelPolicy<RandomWalkAction, DoubleVector, RandomWalkState, RandomWalkState, PaperPolicyRecord> {

//    private Predictor<RandomWalkState> perfectPredictor;

    public RandomWalkPolicy(SplittableRandom random) {
        super(random);
    }

//    @Override
//    public double[] getActionProbabilityDistribution(RandomWalkState gameState) {
//        if(perfectPredictor == null) {
//            perfectPredictor = gameState.getKnownModelWithPerfectObservationPredictor();
//        }
//        return perfectPredictor.apply(gameState);
//    }
//
//    @Override
//    public RandomWalkAction getDiscreteAction(RandomWalkState gameState) {
//        if(perfectPredictor == null) {
//            perfectPredictor = gameState.getKnownModelWithPerfectObservationPredictor();
//        }
//        var actions = gameState.getPossibleOpponentActions();
//        var probabilities = perfectPredictor.apply(gameState);
//        return actions[RandomDistributionUtils.getRandomIndexFromDistribution(probabilities, random)];
//    }
//
//    @Override
//    public void updateStateOnPlayedActions(List<RandomWalkAction> opponentActionList) {
//        // This is it
//    }
//
//    @Override
//    public PaperPolicyRecord getPolicyRecord(RandomWalkState gameState) {
//        return new PaperPolicyRecord(null, null, 0, 0, 0, 0);
//    }
}
