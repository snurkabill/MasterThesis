package vahy.impl.testdomain.emptySpace;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVector;

import java.util.Arrays;
import java.util.List;

public class EmptySpaceState implements State<EmptySpaceAction, DoubleVector, EmptySpaceState, EmptySpaceState>, Observation {

    private final boolean isPlayerTurn;

    public EmptySpaceState(boolean isPlayerTurn) {
        this.isPlayerTurn = isPlayerTurn;
    }

    @Override
    public EmptySpaceAction[] getAllPossibleActions() {
        if(isPlayerTurn) {
            return EmptySpaceAction.playerActions;
        } else {
            return  EmptySpaceAction.opponentActions;
        }
    }

    @Override
    public EmptySpaceAction[] getPossiblePlayerActions() {
        return getAllPossibleActions();
    }

    @Override
    public EmptySpaceAction[] getPossibleOpponentActions() {
        return getAllPossibleActions();
    }

    @Override
    public StateRewardReturn<EmptySpaceAction, DoubleVector, EmptySpaceState, EmptySpaceState> applyAction(EmptySpaceAction actionType) {
        return new ImmutableStateRewardReturnTuple<>(new EmptySpaceState(!isPlayerTurn), 0.0);
    }

    @Override
    public DoubleVector getPlayerObservation() {
        return new DoubleVector(new double[] {0.0});
    }

    @Override
    public EmptySpaceState getOpponentObservation() {
        return this;
    }

    @Override
    public Predictor<EmptySpaceState> getKnownModelWithPerfectObservationPredictor() {
        return new Predictor<>() {
            private double[] fixedPrediction = new double[]{1 / 3., 2 / 3.0};

            @Override
            public double[] apply(EmptySpaceState observation) {
                return fixedPrediction;
            }

            @Override
            public double[][] apply(EmptySpaceState[] observationArray) {
                var prediction = new double[observationArray.length][];
                Arrays.fill(prediction, fixedPrediction);
                return prediction;
            }
        };
    }

    @Override
    public String readableStringRepresentation() {
        return null;
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
    public boolean isOpponentTurn() {
        return !isPlayerTurn;
    }

    @Override
    public boolean isFinalState() {
        return false;
    }

}
