package vahy.testDomain.model;

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

    private static final int TEST_STATE_SIZE = 10;

    public static final TestAction[] PLAYER_ACTIONS = new TestAction[] {TestAction.A, TestAction.B, TestAction.C};
    public static final TestAction[] OPPONENT_ACTIONS = new TestAction[] {TestAction.X, TestAction.Y, TestAction.Z};

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

    private boolean isOpponentTurn() {
        char c = internalState.get(internalState.size() - 1);
        if(Arrays.stream(OPPONENT_ACTIONS).anyMatch(testAction -> c == testAction.getCharRepresentation())) {
            return true;
        } else if(Arrays.stream(PLAYER_ACTIONS).anyMatch(testAction -> c == testAction.getCharRepresentation())) {
            return false;
        } else {
            throw new IllegalArgumentException("Not known state: " + c);
        }
    }


    @Override
    public TestAction[] getAllPossibleActions() {
        if(isOpponentTurn()) {
            return OPPONENT_ACTIONS;
        } else {
            return PLAYER_ACTIONS;
        }
    }

    @Override
    public int getTotalEntityCount() {
        return 2;
    }

    @Override
    public StateRewardReturn<TestAction, DoubleVector, TestState> applyAction(TestAction action) {
        List<Character> newInternalState = new ArrayList<>(internalState);
        newInternalState.add(action.getCharRepresentation());
        return new ImmutableStateRewardReturn<>(new TestState(newInternalState), new double[] {action.getReward(), 0.0});
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
    public boolean isFinalState() {
        return internalState.size() == TEST_STATE_SIZE;
    }

    @Override
    public DoubleVector getInGameEntityObservation(int inGameEntityId) {
        return getCommonObservation(0);
    }

    @Override
    public DoubleVector getCommonObservation(int inGameEntityId) {
        var doubleArray = new double[TEST_STATE_SIZE];
        for (int i = 0; i < internalState.size(); i++) {
            doubleArray[i] = (double) internalState.get(i);
        }
        return new DoubleVector(doubleArray);
    }

    @Override
    public Predictor<TestState> getKnownModelWithPerfectObservationPredictor() {
        return null;
    }

    @Override
    public int getInGameEntityIdOnTurn() {
        return isOpponentTurn() ? 0 : 1;
    }

    @Override
    public boolean isInGame(int inGameEntityId) {
        return true;
    }
}
