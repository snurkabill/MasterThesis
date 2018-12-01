package vahy.testDomain.model;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleReward;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestState implements State<TestAction, DoubleReward, DoubleVectorialObservation, TestState> {

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
        if(isOpponentTurn()) {
            return TestAction.playerActions;
        } else {
            return TestAction.opponentActions;
        }
    }

    @Override
    public StateRewardReturn<TestAction, DoubleReward, DoubleVectorialObservation, TestState> applyAction(TestAction action) {
        List<Character> newInternalState = new ArrayList<>(internalState);
        newInternalState.add(action.getCharRepresentation());
        return new ImmutableStateRewardReturnTuple<>(
            new TestState(newInternalState),
            new DoubleReward(action.getReward()));
    }

    @Override
    public TestState deepCopy() {
        throw new UnsupportedOperationException("");
    }

    @Override
    public DoubleVectorialObservation getObservation() {
        return new DoubleVectorialObservation(internalState.stream().mapToDouble(Character::getNumericValue).toArray());
    }

    @Override
    public String readableStringRepresentation() {
        return internalState.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    @Override
    public boolean isOpponentTurn() {
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
