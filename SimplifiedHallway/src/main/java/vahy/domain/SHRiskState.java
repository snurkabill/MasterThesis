package vahy.domain;

import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
import vahy.examples.simplifiedHallway.SHAction;
import vahy.examples.simplifiedHallway.SHState;
import vahy.impl.model.ImmutableStateRewardReturn;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;

import java.util.List;

public class SHRiskState implements PaperState<SHAction, DoubleVector, SHRiskState>, Observation {

    private final SHState innerState;

    public SHRiskState(SHState innerState) {
        this.innerState = innerState;
    }

    @Override
    public boolean isRiskHit(int playerId) {
        return innerState.isAgentKilled();
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

    @Override
    public Predictor<SHRiskState> getKnownModelWithPerfectObservationPredictor() {
        return new Predictor<SHRiskState>() {

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
        };
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
    public boolean isInGame(int inGameEntityId) {
        return innerState.isInGame(inGameEntityId);
    }

    @Override
    public boolean isFinalState() {
        return innerState.isFinalState();
    }
}
