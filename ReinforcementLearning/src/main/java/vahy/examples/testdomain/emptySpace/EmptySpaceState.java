package vahy.examples.testdomain.emptySpace;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
import vahy.impl.model.ImmutableStateRewardReturn;
import vahy.impl.model.observation.DoubleVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

public class EmptySpaceState implements Observation, State<EmptySpaceAction, DoubleVector, EmptySpaceState> {

    static final DoubleVector FIXED_OBSERVATION = new DoubleVector(new double[] {0.0});
    static final double[] FIXED_REWARD = new double[] {0.0, 0.0};

    // for performance purposes
    public static final EnumMap<EmptySpaceAction, EmptySpaceAction[]> OBSERVED_ACTION_MAP = new EnumMap<EmptySpaceAction, EmptySpaceAction[]>(EmptySpaceAction.class);
    static {
        for (EmptySpaceAction value : EmptySpaceAction.values()) {
            OBSERVED_ACTION_MAP.put(value,  new EmptySpaceAction[] {value, value});
        }
    }

    protected final boolean changeTurn;
    protected final boolean isPlayerTurn;

    public EmptySpaceState(boolean changeTurn, boolean isPlayerTurn) {
        this.changeTurn = changeTurn;
        this.isPlayerTurn = isPlayerTurn;
    }

    @Override
    public EmptySpaceAction[] getAllPossibleActions(int inGameEntityId) {
        if(isPlayerTurn) {
            return EmptySpaceAction.playerActions;
        } else {
            return  EmptySpaceAction.opponentActions;
        }
    }

    @Override
    public int getTotalEntityCount() {
        return 2;
    }

    @Override
    public StateRewardReturn<EmptySpaceAction, DoubleVector, EmptySpaceState> applyAction(EmptySpaceAction actionType) {
        return new ImmutableStateRewardReturn<>(new EmptySpaceState(!changeTurn, changeTurn ? !isPlayerTurn : isPlayerTurn), FIXED_REWARD, OBSERVED_ACTION_MAP.get(actionType));
    }

    @Override
    public DoubleVector getInGameEntityObservation(int inGameEntityId) {
        return getCommonObservation(0);
    }

    @Override
    public DoubleVector getCommonObservation(int playerId) {
        return FIXED_OBSERVATION;
    }

    @Override
    public Predictor<EmptySpaceState> getKnownModelWithPerfectObservationPredictor() {
        return new Predictor<>() {
            private final double[] fixedPrediction = new double[]{1 / 3., 2 / 3.0};

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

            @Override
            public List<double[]> apply(List<EmptySpaceState> observationArray) {
                var output = new ArrayList<double[]>(observationArray.size());
                for (int i = 0; i < observationArray.size(); i++) {
                    output.add(apply(observationArray.get(i)));
                }
                return output;
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
    public int getInGameEntityIdOnTurn() {
        return isPlayerTurn ? 0 : 1;
    }

    @Override
    public boolean isEnvironmentEntityOnTurn() {
        return !isPlayerTurn;
    }

    @Override
    public boolean isInGame(int playerId) {
        return true;
    }

    @Override
    public boolean isFinalState() {
        return false;
    }

}
