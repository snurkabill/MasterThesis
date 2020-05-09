package vahy.impl.testdomain.model;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
import vahy.impl.model.ImmutableStateRewardReturn;
import vahy.impl.model.observation.DoubleVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestState implements State<TestAction, DoubleVector, TestState>, Observation {

    public static TestState getDefaultInitialStatePlayerTurn() {
        return new TestState(Collections.singletonList('Z'));
    }

    public static TestState getDefaultInitialStateOpponentTurn() {
        return new TestState(Collections.singletonList('A'));
    }

    private final List<Character> internalState;

    public TestState(List<Character> internalState) {
        this.internalState = internalState;
    }

    @Override
    public TestAction[] getAllPossibleActions() {
        if(isPlayerZeroOnTurn()) {
            return TestAction.playerActions;
        } else {
            return TestAction.opponentActions;
        }
    }

    @Override
    public StateRewardReturn<TestAction, DoubleVector, TestState> applyAction(TestAction action) {
        List<Character> newInternalState = new ArrayList<>(internalState);
        newInternalState.add(action.getCharRepresentation());
        return new ImmutableStateRewardReturn<>(
            new TestState(newInternalState),
            action.getReward());
    }

    @Override
    public DoubleVector getPlayerObservation(int playerId) {
        return new DoubleVector(internalState.stream().mapToDouble(Character::getNumericValue).toArray());
    }

    @Override
    public DoubleVector getCommonObservation(int playerId) {
        return new DoubleVector(internalState.stream().mapToDouble(Character::getNumericValue).toArray());
    }

    @Override
    public Predictor<TestState> getKnownModelWithPerfectObservationPredictor() {
        throw new UnsupportedOperationException("Full known model predictor is not supported");
    }

    @Override
    public String readableStringRepresentation() {
        return internalState.stream().map(Object::toString).collect(Collectors.joining(", "));
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
    public int getTotalPlayerCount() {
        return 2;
    }

    @Override
    public int getPlayerIdOnTurn() {
        return isPlayerZeroOnTurn() ? 0 : 1;
    }

    @Override
    public boolean isInGame(int playerId) {
        return false;
    }


    private boolean isPlayerZeroOnTurn() {
        char c = internalState.get(internalState.size() - 1);
        if(Arrays.stream(TestAction.opponentActions).anyMatch(testAction -> c == testAction.getCharRepresentation())) {
            return false;
        } else if(Arrays.stream(TestAction.playerActions).anyMatch(testAction -> c == testAction.getCharRepresentation())) {
            return true;
        } else {
            throw new IllegalArgumentException("Not known state: " + c);
        }
    }

    @Override
    public boolean isFinalState() {
        return internalState.size() > 10;
    }
}
