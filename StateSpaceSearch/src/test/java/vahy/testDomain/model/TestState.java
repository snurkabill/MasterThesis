package vahy.testDomain.model;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestState implements State<TestAction, DoubleScalarReward, DoubleVectorialObservation, TestState> {

    private final List<Character> internalState;

    public TestState(List<Character> internalState) {
        this.internalState = internalState;
    }

    @Override
    public TestAction[] getAllPossibleActions() {
        if(isOpponentTurn()) {
            return TestAction.playerActions;
        } else {
            return TestAction.environmentActions;
        }
    }

    @Override
    public StateRewardReturn<TestAction, DoubleScalarReward, DoubleVectorialObservation, TestState> applyAction(TestAction action) {
        List<Character> newInternalState = new ArrayList<>(internalState);
        newInternalState.add(action.getCharRepresentation());
        return new ImmutableStateRewardReturnTuple<>(
            new TestState(newInternalState),
            new DoubleScalarReward(action.getReward()));
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
        if(c == 'A' || c == 'B' || c == 'C') {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isFinalState() {
        return internalState.size() > 10;
    }
}
