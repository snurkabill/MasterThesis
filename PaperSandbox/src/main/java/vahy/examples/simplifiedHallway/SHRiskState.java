package vahy.examples.simplifiedHallway;

import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
import vahy.impl.model.ImmutableStateRewardReturn;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;

import java.util.ArrayList;
import java.util.List;

public class SHRiskState implements PaperState<SHAction, DoubleVector, SHRiskState>, Observation {

    private static final boolean[] NO_RISK_ARRAY = new boolean[] {false, false};
    private static final boolean[] RISK_HIT_ARRAY = new boolean[] {false, true};

    private final SHState innerState;

    public SHRiskState(SHState innerState) {
        this.innerState = innerState;
    }

    @Override
    public boolean isRiskHit(int playerId) {
        return innerState.isAgentKilled();
    }

    @Override
    public boolean[] getRiskVector() {
        return innerState.isAgentKilled() ? RISK_HIT_ARRAY : NO_RISK_ARRAY;
    }

    @Override
    public SHAction[] getAllPossibleActions() {
        return innerState.getAllPossibleActions();
    }

    @Override
    public int getTotalEntityCount() {
        return innerState.getTotalEntityCount();
    }

    @Override
    public StateRewardReturn<SHAction, DoubleVector, SHRiskState> applyAction(SHAction actionType) {
        var applied = innerState.applyAction(actionType);
        return new ImmutableStateRewardReturn<>(new SHRiskState(applied.getState()), applied.getReward());
    }

    @Override
    public DoubleVector getInGameEntityObservation(int inGameEntityId) {
        return innerState.getInGameEntityObservation(inGameEntityId);
    }

    @Override
    public DoubleVector getCommonObservation(int inGameEntityId) {
        return innerState.getCommonObservation(inGameEntityId);
    }

    private static class PerfectShRiskPredictor implements Predictor<SHRiskState> {

        private Predictor<SHState> innerPredictor;

        @Override
        public double[] apply(SHRiskState observation) {
            if(innerPredictor == null) {
                innerPredictor = observation.innerState.getKnownModelWithPerfectObservationPredictor();
            }
            return innerPredictor.apply(observation.innerState);
        }

        @Override
        public double[][] apply(SHRiskState[] observationArray) {
            if(innerPredictor == null) {
                innerPredictor = observationArray[0].innerState.getKnownModelWithPerfectObservationPredictor();
            }
            var innerStateObservationArray = new SHState[observationArray.length];
            for (int i = 0; i < innerStateObservationArray.length; i++) {
                innerStateObservationArray[i] = observationArray[i].innerState;
            }
            return innerPredictor.apply(innerStateObservationArray);
        }

        @Override
        public List<double[]> apply(List<SHRiskState> observationArray) {
            var output = new ArrayList<double[]>(observationArray.size());
            for (int i = 0; i < observationArray.size(); i++) {
                output.add(apply(observationArray.get(i)));
            }
            return output;
        }
    };

    @Override
    public Predictor<SHRiskState> getKnownModelWithPerfectObservationPredictor() {
        return new PerfectShRiskPredictor();
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
        if (!(o instanceof SHRiskState)) return false;

        SHRiskState that = (SHRiskState) o;

        return innerState.equals(that.innerState);
    }

    @Override
    public int hashCode() {
        return innerState.hashCode();
    }
}
