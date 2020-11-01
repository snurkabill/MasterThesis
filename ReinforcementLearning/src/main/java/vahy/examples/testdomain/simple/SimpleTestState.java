package vahy.examples.testdomain.simple;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.predictor.PerfectStatePredictor;
import vahy.impl.model.ImmutableStateRewardReturn;
import vahy.impl.model.observation.DoubleVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimpleTestState implements State<SimpleTestAction, DoubleVector, SimpleTestState> {

    private static final int TEST_STATE_SIZE = 10;

    static final SimpleTestAction[] PLAYER_ACTIONS = new SimpleTestAction[] {SimpleTestAction.A, SimpleTestAction.B, SimpleTestAction.C};
    static final SimpleTestAction[] OPPONENT_ACTIONS = new SimpleTestAction[] {SimpleTestAction.X, SimpleTestAction.Y, SimpleTestAction.Z};

    static final Map<SimpleTestAction, SimpleTestAction[]> observedActionMap = Map.of(
        SimpleTestAction.A, new SimpleTestAction[] {SimpleTestAction.A, SimpleTestAction.A},
        SimpleTestAction.B, new SimpleTestAction[] {SimpleTestAction.B, SimpleTestAction.B},
        SimpleTestAction.C, new SimpleTestAction[] {SimpleTestAction.C, SimpleTestAction.C},
        SimpleTestAction.X, new SimpleTestAction[] {SimpleTestAction.X, SimpleTestAction.X},
        SimpleTestAction.Y, new SimpleTestAction[] {SimpleTestAction.Y, SimpleTestAction.Y},
        SimpleTestAction.Z, new SimpleTestAction[] {SimpleTestAction.Z, SimpleTestAction.Z}
        );

    private final boolean isPlayerTurn;
    private final List<Character> actionSequence;

    public SimpleTestState(boolean isPlayerTurn, List<Character> actionSequence) {
        this.isPlayerTurn = isPlayerTurn;
        this.actionSequence = actionSequence;
    }


    @Override
    public SimpleTestAction[] getAllPossibleActions(int inGameEntityId) {
        if(isPlayerTurn) {
            return PLAYER_ACTIONS;
        } else {
            return OPPONENT_ACTIONS;
        }
    }

    @Override
    public int getTotalEntityCount() {
        return 2;
    }

    @Override
    public StateRewardReturn<SimpleTestAction, DoubleVector, SimpleTestState> applyAction(SimpleTestAction action) {
        List<Character> newInternalState = new ArrayList<>(actionSequence);
        newInternalState.add(action.getCharRepresentation());
        return new ImmutableStateRewardReturn<>(new SimpleTestState(!isPlayerTurn, newInternalState), new double[] {-action.getReward(), action.getReward()}, observedActionMap.get(action));
    }

    @Override
    public String readableStringRepresentation() {
        return actionSequence.stream().map(Object::toString).collect(Collectors.joining(", "));
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
        return actionSequence.size() == TEST_STATE_SIZE;
    }

    @Override
    public DoubleVector getInGameEntityObservation(int inGameEntityId) {
        return getCommonObservation(0);
    }

    @Override
    public DoubleVector getCommonObservation(int inGameEntityId) {
        var doubleArray = new double[TEST_STATE_SIZE];
        for (int i = 0; i < actionSequence.size(); i++) {
            doubleArray[i] = (double) actionSequence.get(i);
        }
        return new DoubleVector(doubleArray);
    }

    @Override
    public PerfectStatePredictor<SimpleTestAction, DoubleVector, SimpleTestState> getKnownModelWithPerfectObservationPredictor() {
        return null;
    }

    @Override
    public int getInGameEntityIdOnTurn() {
        return isPlayerTurn ? 1 : 0;
    }

    @Override
    public boolean isEnvironmentEntityOnTurn() {
        return false;
    }

    @Override
    public boolean isInGame(int inGameEntityId) {
        return true;
    }
}
