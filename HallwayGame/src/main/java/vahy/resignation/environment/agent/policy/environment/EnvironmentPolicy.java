package vahy.resignation.environment.agent.policy.environment;

import vahy.api.policy.PolicyRecord;
import vahy.api.predictor.Predictor;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.RandomizedPolicy;
import vahy.resignation.environment.HallwayActionWithResign;
import vahy.resignation.environment.state.HallwayStateWithResign;
import vahy.utils.RandomDistributionUtils;

import java.util.List;
import java.util.SplittableRandom;

public class EnvironmentPolicy<TPolicyRecord extends PolicyRecord> extends RandomizedPolicy<HallwayActionWithResign, DoubleVector, HallwayStateWithResign, HallwayStateWithResign, TPolicyRecord> {

    private Predictor<HallwayStateWithResign> perfectPredictor;

    public EnvironmentPolicy(SplittableRandom random) {
        super(random);
    }

    @Override
    public double[] getActionProbabilityDistribution(HallwayStateWithResign gameState) {
        if(perfectPredictor == null) {
            perfectPredictor = gameState.getKnownModelWithPerfectObservationPredictor();
        }
        return perfectPredictor.apply(gameState);
    }

    @Override
    public HallwayActionWithResign getDiscreteAction(HallwayStateWithResign gameState) {
        if(perfectPredictor == null) {
            perfectPredictor = gameState.getKnownModelWithPerfectObservationPredictor();
        }
        var actions = gameState.getPossibleOpponentActions();
        var probabilities = perfectPredictor.apply(gameState);
        return actions[RandomDistributionUtils.getRandomIndexFromDistribution(probabilities, random)];
    }

    @Override
    public void updateStateOnPlayedActions(List<HallwayActionWithResign> opponentActionList) {
        // this is it
    }

    @Override
    public TPolicyRecord getPolicyRecord(HallwayStateWithResign gameState) {
        return null;
    }
}
