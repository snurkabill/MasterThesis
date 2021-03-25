package vahy.examples.coqnuering;

import vahy.RiskState;
import vahy.api.model.StateRewardReturn;
import vahy.api.predictor.PerfectStatePredictor;
import vahy.examples.conquering.ConqueringAction;
import vahy.examples.conquering.ConqueringState;
import vahy.impl.model.ImmutableStateRewardReturn;
import vahy.impl.model.observation.DoubleVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConqueringRiskState implements RiskState<ConqueringAction, DoubleVector, ConqueringRiskState> {

    private final ConqueringState innerState;
    private final boolean[] riskArray;

    public ConqueringRiskState(ConqueringState innerState) {
        this.innerState = innerState;
        var isEliminatedArray = innerState.getIsEntityEliminatedArray();
        this.riskArray = Arrays.copyOf(isEliminatedArray, isEliminatedArray.length);
    }

    @Override
    public boolean isRiskHit(int playerId) {
        return riskArray[playerId];
    }

    @Override
    public boolean[] getRiskVector() {
        return riskArray;
    }

    @Override
    public ConqueringAction[] getAllPossibleActions(int inGameEntityId) {
        return innerState.getAllPossibleActions(inGameEntityId);
    }

    @Override
    public int getTotalEntityCount() {
        return innerState.getTotalEntityCount();
    }

    @Override
    public StateRewardReturn<ConqueringAction, DoubleVector, ConqueringRiskState> applyAction(ConqueringAction actionType) {
        var applied = innerState.applyAction(actionType);
        return new ImmutableStateRewardReturn<>(new ConqueringRiskState(applied.getState()), applied.getReward(), applied.getAction());
    }

    @Override
    public DoubleVector getInGameEntityObservation(int inGameEntityId) {
        return innerState.getInGameEntityObservation(inGameEntityId);
    }

    @Override
    public DoubleVector getCommonObservation(int inGameEntityId) {
        return innerState.getCommonObservation(inGameEntityId);
    }

    private static class PerfectConqueringPredictor implements PerfectStatePredictor<ConqueringAction, DoubleVector, ConqueringRiskState> {

        private PerfectStatePredictor<ConqueringAction, DoubleVector, ConqueringState> innerPredictor;

        @Override
        public double[] apply(ConqueringRiskState observation) {
            if(innerPredictor == null) {
                innerPredictor = observation.innerState.getKnownModelWithPerfectObservationPredictor();
            }
            return innerPredictor.apply(observation.innerState);
        }

        @Override
        public double[][] apply(ConqueringRiskState[] observationArray) {
            if(innerPredictor == null) {
                innerPredictor = observationArray[0].innerState.getKnownModelWithPerfectObservationPredictor();
            }
            var innerStateObservationArray = new ConqueringState[observationArray.length];
            for (int i = 0; i < innerStateObservationArray.length; i++) {
                innerStateObservationArray[i] = observationArray[i].innerState;
            }
            return innerPredictor.apply(innerStateObservationArray);
        }

        @Override
        public List<double[]> apply(List<ConqueringRiskState> observationArray) {
            var output = new ArrayList<double[]>(observationArray.size());
            for (int i = 0; i < observationArray.size(); i++) {
                output.add(apply(observationArray.get(i)));
            }
            return output;
        }
    }

    @Override
    public PerfectStatePredictor<ConqueringAction, DoubleVector, ConqueringRiskState> getKnownModelWithPerfectObservationPredictor() {
        return new ConqueringRiskState.PerfectConqueringPredictor();
    }

    @Override
    public String readableStringRepresentation() {
        return innerState.readableStringRepresentation();
    }

    @Override
    public List<String> getCsvHeader() {
        return innerState.getCsvHeader();
    }

    @Override
    public List<String> getCsvRecord() {
        return innerState.getCsvRecord();
    }

    @Override
    public int getInGameEntityIdOnTurn() {
        return innerState.getInGameEntityIdOnTurn();
    }

    @Override
    public boolean isEnvironmentEntityOnTurn() {
        return innerState.isEnvironmentEntityOnTurn();
    }

    @Override
    public boolean isInGame(int inGameEntityId) {
        return innerState.isInGame(inGameEntityId);
    }

    @Override
    public boolean isFinalState() {
        return innerState.isFinalState();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof  ConqueringRiskState)) return false;

        ConqueringRiskState that = (ConqueringRiskState) o;

        if (!innerState.equals(that.innerState)) return false;
        return Arrays.equals(riskArray, that.riskArray);
    }

    @Override
    public int hashCode() {
        int result = innerState.hashCode();
        result = 31 * result + Arrays.hashCode(riskArray);
        return result;
    }
}
