package vahy.impl.testdomain.emptySpace;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVector;

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
    public EmptySpaceState deepCopy() {
        return null;
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
