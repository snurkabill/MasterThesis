package vahy.examples.bomberman;

import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
import vahy.impl.model.ImmutableStateRewardReturn;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BomberManRiskState implements PaperState<BomberManAction, DoubleVector, BomberManRiskState>, Observation {

    private final BomberManState innerState;
    private final boolean[] riskArray;

    public BomberManRiskState(BomberManState innerState) {
        this.innerState = innerState;
        this.riskArray = new boolean[innerState.getTotalEntityCount()];
        for (int i = 0; i < riskArray.length; i++) {
            riskArray[i] = !innerState.isInGame(i);
        }
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
    public BomberManAction[] getAllPossibleActions() {
        return innerState.getAllPossibleActions();
    }

    @Override
    public int getTotalEntityCount() {
        return innerState.getTotalEntityCount();
    }

    @Override
    public StateRewardReturn<BomberManAction, DoubleVector, BomberManRiskState> applyAction(BomberManAction actionType) {
        var applied = innerState.applyAction(actionType);
        return new ImmutableStateRewardReturn<>(new BomberManRiskState(applied.getState()), applied.getReward());
    }

    @Override
    public DoubleVector getInGameEntityObservation(int inGameEntityId) {
        return innerState.getInGameEntityObservation(inGameEntityId);
    }

    @Override
    public DoubleVector getCommonObservation(int inGameEntityId) {
        return innerState.getCommonObservation(inGameEntityId);
    }

    private static class PerfectBomberManPredictor implements Predictor<BomberManRiskState> {

        private Predictor<BomberManState> innerPredictor;

        @Override
        public double[] apply(BomberManRiskState observation) {
            if(innerPredictor == null) {
                innerPredictor = observation.innerState.getKnownModelWithPerfectObservationPredictor();
            }
            return innerPredictor.apply(observation.innerState);
        }

        @Override
        public double[][] apply(BomberManRiskState[] observationArray) {
            if(innerPredictor == null) {
                innerPredictor = observationArray[0].innerState.getKnownModelWithPerfectObservationPredictor();
            }
            var innerStateObservationArray = new BomberManState[observationArray.length];
            for (int i = 0; i < innerStateObservationArray.length; i++) {
                innerStateObservationArray[i] = observationArray[i].innerState;
            }
            return innerPredictor.apply(innerStateObservationArray);
        }

        @Override
        public List<double[]> apply(List<BomberManRiskState> observationArray) {
            var output = new ArrayList<double[]>(observationArray.size());
            for (int i = 0; i < observationArray.size(); i++) {
                output.add(apply(observationArray.get(i)));
            }
            return output;
        }
    }

    @Override
    public Predictor<BomberManRiskState> getKnownModelWithPerfectObservationPredictor() {
        return new PerfectBomberManPredictor();
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
        if (!(o instanceof  BomberManRiskState)) return false;

        BomberManRiskState that = (BomberManRiskState) o;

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
