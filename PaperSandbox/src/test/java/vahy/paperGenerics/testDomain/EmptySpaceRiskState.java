package vahy.paperGenerics.testDomain;

import vahy.RiskState;
import vahy.api.model.StateRewardReturn;
import vahy.api.predictor.PerfectStatePredictor;
import vahy.examples.testdomain.emptySpace.EmptySpaceAction;
import vahy.examples.testdomain.emptySpace.EmptySpaceState;
import vahy.impl.model.ImmutableStateRewardReturn;
import vahy.impl.model.observation.DoubleVector;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public class EmptySpaceRiskState implements RiskState<EmptySpaceAction, DoubleVector, EmptySpaceRiskState> {

    private static final boolean[] NO_RISK_ARRAY = new boolean[] {false, false};
    private static final boolean[] RISK_HIT_ARRAY = new boolean[] {true, true};

    private final EmptySpaceState innerState;
    private final SplittableRandom random;
    private final boolean isRiskHit;
    private final double riskProbability;

    public EmptySpaceRiskState(EmptySpaceState innerState, SplittableRandom random,  boolean isRiskHit, double riskProbability) {
        this.innerState = innerState;
        this.random = random;
        this.riskProbability = riskProbability;
        this.isRiskHit = isRiskHit;
    }

    @Override
    public EmptySpaceAction[] getAllPossibleActions(int inGameEntityId) {
        return innerState.getAllPossibleActions(inGameEntityId);
    }

    @Override
    public int getTotalEntityCount() {
        return innerState.getTotalEntityCount();
    }

    @Override
    public StateRewardReturn<EmptySpaceAction, DoubleVector, EmptySpaceRiskState> applyAction(EmptySpaceAction actionType) {
        var applied = innerState.applyAction(actionType);
        return new ImmutableStateRewardReturn<>(new EmptySpaceRiskState(applied.getState(), random, random.nextDouble() < riskProbability, riskProbability), new double[] {random.nextDouble(), random.nextDouble()}, applied.getAction());
    }

    @Override
    public DoubleVector getInGameEntityObservation(int inGameEntityId) {
        return innerState.getInGameEntityObservation(inGameEntityId);
    }

    @Override
    public DoubleVector getCommonObservation(int inGameEntityId) {
        return innerState.getCommonObservation(inGameEntityId);
    }

    @Override
    public PerfectStatePredictor<EmptySpaceAction, DoubleVector, EmptySpaceRiskState> getKnownModelWithPerfectObservationPredictor() {
        return new PerfectStatePredictor<EmptySpaceAction, DoubleVector, EmptySpaceRiskState>() {

            private PerfectStatePredictor<EmptySpaceAction, DoubleVector, EmptySpaceState> innerPredictor;

            @Override
            public double[] apply(EmptySpaceRiskState observation) {
                if(innerPredictor == null) {
                    innerPredictor = observation.innerState.getKnownModelWithPerfectObservationPredictor();
                }
                return innerPredictor.apply(observation.innerState);
            }

            @Override
            public double[][] apply(EmptySpaceRiskState[] observationArray) {
                var prediction = new double[observationArray.length][];
                for (int i = 0; i < prediction.length; i++) {
                    prediction[i] = apply(observationArray[i]);
                }
                return prediction;
            }

            @Override
            public List<double[]> apply(List<EmptySpaceRiskState> observationArray) {
                var output = new ArrayList<double[]>(observationArray.size());
                for (int i = 0; i < observationArray.size(); i++) {
                    output.add(apply(observationArray.get(i)));
                }
                return output;
            }
        };
    }

    @Override
    public String readableStringRepresentation() {
        return "NOT IMPLEMENTED - readableStringRepresentation";
    }

    @Override
    public List<String> getCsvHeader() {
        return null;
    }

    @Override
    public List<String> getCsvRecord() {
        return null;
    }

    @Override
    public int getInGameEntityIdOnTurn() {
        return innerState.getInGameEntityIdOnTurn();
    }

    @Override
    public boolean isEnvironmentEntityOnTurn() {
        return false;
    }

    @Override
    public boolean isInGame(int inGameEntityId) {
        return innerState.isInGame(inGameEntityId);
    }

    @Override
    public boolean isFinalState() {
        return isRiskHit;
    }

    @Override
    public boolean isRiskHit(int playerId) {
        return isRiskHit;
    }

    @Override
    public boolean[] getRiskVector() {
        return isRiskHit ? RISK_HIT_ARRAY : NO_RISK_ARRAY;
    }
}
