package vahy.examples.tictactoe;

import vahy.api.model.StateRewardReturn;
import vahy.api.predictor.PerfectStatePredictor;
import vahy.impl.model.ImmutableStateRewardReturn;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;

import java.util.ArrayList;
import java.util.List;

public class TicTacToeRiskState implements PaperState<TicTacToeAction, DoubleVector, TicTacToeRiskState> {

    private static final boolean[] NO_RISK_ARRAY = new boolean[] {false, false};
    private static final boolean[] RISK_HIT_ARRAY = new boolean[] {false, true};

    private final TicTacToeState innerState;

    public TicTacToeRiskState(TicTacToeState innerState) {
        this.innerState = innerState;
    }

    @Override
    public boolean isRiskHit(int playerId) {
        if(innerState.isFinalState()) {
            var ticTacToeResult = innerState.getResult();
            if(playerId == 0) {
                return ticTacToeResult == TicTacToeResult.WIN_1;
            } else if(playerId == 1) {
                return ticTacToeResult == TicTacToeResult.WIN_0;
            } else {
                throw new IllegalStateException("Not expected player id: [" + playerId + "]");
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean[] getRiskVector() {
        return isFinalState() ? RISK_HIT_ARRAY : NO_RISK_ARRAY;
    }

    @Override
    public TicTacToeAction[] getAllPossibleActions(int inGameEntityId) {
        return innerState.getAllPossibleActions(inGameEntityId);
    }

    @Override
    public int getTotalEntityCount() {
        return innerState.getTotalEntityCount();
    }

    @Override
    public StateRewardReturn<TicTacToeAction, DoubleVector, TicTacToeRiskState> applyAction(TicTacToeAction actionType) {
        var applied = innerState.applyAction(actionType);
        return new ImmutableStateRewardReturn<>(new TicTacToeRiskState(applied.getState()), applied.getReward(), applied.getAction());
    }

    @Override
    public DoubleVector getInGameEntityObservation(int inGameEntityId) {
        return innerState.getInGameEntityObservation(inGameEntityId);
    }

    @Override
    public DoubleVector getCommonObservation(int inGameEntityId) {
        return innerState.getCommonObservation(inGameEntityId);
    }

    private static class PerfectTicTacToePredictor implements PerfectStatePredictor<TicTacToeAction, DoubleVector, TicTacToeRiskState> {

        private PerfectStatePredictor<TicTacToeAction, DoubleVector, TicTacToeState> innerPredictor;

        @Override
        public double[] apply(TicTacToeRiskState observation) {
            if(innerPredictor == null) {
                innerPredictor = observation.innerState.getKnownModelWithPerfectObservationPredictor();
            }
            return innerPredictor.apply(observation.innerState);
        }

        @Override
        public double[][] apply(TicTacToeRiskState[] observationArray) {
            if(innerPredictor == null) {
                innerPredictor = observationArray[0].innerState.getKnownModelWithPerfectObservationPredictor();
            }
            var innerStateObservationArray = new TicTacToeState[observationArray.length];
            for (int i = 0; i < innerStateObservationArray.length; i++) {
                innerStateObservationArray[i] = observationArray[i].innerState;
            }
            return innerPredictor.apply(innerStateObservationArray);
        }

        @Override
        public List<double[]> apply(List<TicTacToeRiskState> observationArray) {
            var output = new ArrayList<double[]>(observationArray.size());
            for (int i = 0; i < observationArray.size(); i++) {
                output.add(apply(observationArray.get(i)));
            }
            return output;
        }
    };

    @Override
    public PerfectStatePredictor<TicTacToeAction, DoubleVector, TicTacToeRiskState> getKnownModelWithPerfectObservationPredictor() {
        return new PerfectTicTacToePredictor();
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
}
