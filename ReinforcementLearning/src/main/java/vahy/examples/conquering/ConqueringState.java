package vahy.examples.conquering;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.predictor.PerfectStatePredictor;
import vahy.impl.model.ImmutableStateRewardReturn;
import vahy.impl.model.observation.DoubleVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ConqueringState implements State<ConqueringAction, DoubleVector, ConqueringState>  {

    public static final int ENVIRONMENT_ENTITY_ID = 0;

    static final ConqueringAction[] ENVIRONMENT_ACTION_ARRAY = new ConqueringAction[] {ConqueringAction.KILL, ConqueringAction.PASS};
    static final ConqueringAction[] PLAYER_ACTION_ARRAY = new ConqueringAction[] {ConqueringAction.FORWARD, ConqueringAction.WAIT};

    private final ConqueringStaticPart staticPart;

    private final int[] playerPositionArray;
    private final int[] playerPositionArray_hidden;
    private final boolean[] isInGameArray;
    private final boolean[] isEntityEliminatedArray;
    private final int inGamePlayerCount;

    private final int entityIdOnTurn;
    private final int playerIdOnTurn;
    private final DoubleVector observation;

    private final int stepsDone;

    public ConqueringState(ConqueringStaticPart staticPart, int[] playerPositionArray, int[] playerPositionArray_hidden, boolean[] isInGameArray, int inGamePlayerCount, int entityIdOnTurn, int stepsDone, boolean[] isEntityEliminatedArray) {
        this(staticPart, playerPositionArray, playerPositionArray_hidden, isInGameArray, isEntityEliminatedArray, inGamePlayerCount, entityIdOnTurn, new DoubleVector(Arrays.stream(playerPositionArray).mapToDouble(x -> x / (double) staticPart.getLengthOfHall()).toArray()), stepsDone);
    }

    public ConqueringState(ConqueringStaticPart staticPart, int[] playerPositionArray, int[] playerPositionArray_hidden, boolean[] isInGameArray, boolean[] isEntityEliminatedArray, int inGamePlayerCount, int entityIdOnTurn, DoubleVector observation, int stepsDone) {
        this.staticPart = staticPart;
        this.playerPositionArray = playerPositionArray;
        this.playerPositionArray_hidden = playerPositionArray_hidden;
        this.isInGameArray = isInGameArray;
        this.isEntityEliminatedArray = isEntityEliminatedArray;
        this.inGamePlayerCount = inGamePlayerCount;
        this.entityIdOnTurn = entityIdOnTurn;
        this.playerIdOnTurn = entityIdOnTurn - 1;
        this.observation = observation;
        this.stepsDone = stepsDone;
    }

    @Override
    public ConqueringAction[] getAllPossibleActions(int inGameEntityId) {
        if(entityIdOnTurn == ENVIRONMENT_ENTITY_ID) {
            return ENVIRONMENT_ACTION_ARRAY;
        } else {
            return PLAYER_ACTION_ARRAY;
        }
    }

    @Override
    public int getTotalEntityCount() {
        return staticPart.getTotalEntityCount();
    }

    private int findNextEntityIdOnTurnAfterEnvironment(int currentEntityIdOnTurn, boolean[] array) {
        for (int i = currentEntityIdOnTurn; i < array.length; i++) {
            if(array[i]) {
                return i + 1;
            }
        }
        return ENVIRONMENT_ENTITY_ID; // otherwise environment on turn
    }


    private StateRewardReturn<ConqueringAction, DoubleVector, ConqueringState> playEnvironmentAction(ConqueringAction actionType) {
        if (entityIdOnTurn != ENVIRONMENT_ENTITY_ID) {
            throw new IllegalStateException("Inconsistency. Check");
        }

        var isInGameNewArray = new boolean[isInGameArray.length];
        var rewardArray = new double[staticPart.getTotalEntityCount()];
        var newInGamePlayerCount = 0;
        var attackerCount = 0;
        for (int i = 0; i < isInGameNewArray.length; i++) {
            if(this.playerPositionArray_hidden[i] != 0) {
                newInGamePlayerCount++;
                isInGameNewArray[i] = true;
            } else if(this.playerPositionArray_hidden[i] == 0 && this.isInGameArray[i]) {
                attackerCount++;
            }
        }

        for (int i = 0; i < isInGameNewArray.length; i++) {
            if(this.playerPositionArray_hidden[i] == 0 && this.isInGameArray[i]) {
                rewardArray[i + 1] = actionType == ConqueringAction.PASS ? staticPart.getRewardPerWinning() / (double) attackerCount : -staticPart.getDefaultStepPenalty();
            }
        }


        var nextEntityOnTurn = findNextEntityIdOnTurnAfterEnvironment(this.entityIdOnTurn, isInGameNewArray);

        if(actionType == ConqueringAction.KILL) {

            var isEntityEliminatedArrayCopy = Arrays.copyOf(isEntityEliminatedArray, isEntityEliminatedArray.length);
            for (int i = 0; i < isInGameNewArray.length; i++) {
                isEntityEliminatedArrayCopy[i + 1] = isEntityEliminatedArrayCopy[i + 1] || (this.playerPositionArray_hidden[i] == 0 && this.isInGameArray[i]);
            }

            return new ImmutableStateRewardReturn<>(
                new ConqueringState(
                    staticPart,
                    playerPositionArray_hidden,
                    playerPositionArray_hidden,
                    isInGameNewArray,
                    newInGamePlayerCount,
                    nextEntityOnTurn,
                    stepsDone + 1,
                    isEntityEliminatedArrayCopy),
                rewardArray,
                staticPart.getObservedActionArray(actionType)
            );
        } else if(actionType == ConqueringAction.PASS) {
            return new ImmutableStateRewardReturn<>(
                new ConqueringState(
                    staticPart,
                    playerPositionArray_hidden,
                    playerPositionArray_hidden,
                    isInGameNewArray,
                    newInGamePlayerCount,
                    nextEntityOnTurn,
                    stepsDone + 1,
                    isEntityEliminatedArray),
                rewardArray,
                staticPart.getObservedActionArray(actionType)
            );
        } else {
            throw new IllegalStateException("Unexpected action: [" + actionType + "]");
        }
    }

    private StateRewardReturn<ConqueringAction, DoubleVector, ConqueringState> playPlayerAction(ConqueringAction actionType) {

        var nextEntityOnTurn = findNextEntityIdOnTurnAfterEnvironment(this.entityIdOnTurn, this.isInGameArray);

        if(actionType == ConqueringAction.FORWARD) {
            var newPlayerPositionArray_hidden = Arrays.copyOf(playerPositionArray_hidden, playerPositionArray_hidden.length);
            newPlayerPositionArray_hidden[playerIdOnTurn]--;
            return new ImmutableStateRewardReturn<>(
                new ConqueringState(
                    staticPart,
                    playerPositionArray,
                    newPlayerPositionArray_hidden,
                    isInGameArray,
                    inGamePlayerCount,
                    nextEntityOnTurn,
                    stepsDone + 1, isEntityEliminatedArray),
                staticPart.getActionReward(entityIdOnTurn),
                staticPart.getObservedActionArray(actionType)
            );
        } else if(actionType == ConqueringAction.WAIT) {
            return new ImmutableStateRewardReturn<>(
                new ConqueringState(
                    staticPart,
                    playerPositionArray,
                    playerPositionArray_hidden,
                    isInGameArray,
                    inGamePlayerCount,
                    nextEntityOnTurn,
                    stepsDone + 1, isEntityEliminatedArray),
                staticPart.getActionReward(entityIdOnTurn),
                staticPart.getObservedActionArray(actionType)
            );
        } else {
            throw new IllegalStateException("Unexpected action: [" + actionType + "]");
        }
    }

    @Override
    public StateRewardReturn<ConqueringAction, DoubleVector, ConqueringState> applyAction(ConqueringAction actionType) {
        return actionType.isEnvironmentalAction() ? playEnvironmentAction(actionType) : playPlayerAction(actionType);
    }

    @Override
    public DoubleVector getInGameEntityObservation(int inGameEntityId) {
        return observation;
    }

    @Override
    public DoubleVector getCommonObservation(int inGameEntityId) {
        return observation;
    }

    @Override
    public PerfectStatePredictor<ConqueringAction, DoubleVector, ConqueringState> getKnownModelWithPerfectObservationPredictor() {

        return new PerfectStatePredictor<ConqueringAction, DoubleVector, ConqueringState>() {

            private EnumMap<ConqueringAction, Double> getEnvironmentProbabilities(ConqueringState state) {
                var actionMap = new EnumMap<ConqueringAction, Double>(ConqueringAction.class);

                var baseKillProbability = state.staticPart.getBaseKillProbability();
                var attackingPlayerCount = 0;

                for (int i = 0; i < state.playerPositionArray_hidden.length; i++) {
                    if(state.playerPositionArray_hidden[i] == 0 && state.isInGameArray[i]) {
                        attackingPlayerCount++;
                    }
                }
                var calculatedKillProbability = attackingPlayerCount == 0 ? 0.0 : Math.pow(baseKillProbability, attackingPlayerCount);
                actionMap.put(ConqueringAction.KILL, calculatedKillProbability);
                actionMap.put(ConqueringAction.PASS, 1.0 - calculatedKillProbability);
                return actionMap;
            }

            @Override
            public double[] apply(ConqueringState observation) {
                var probs = getEnvironmentProbabilities(observation);
                var prediction = new double[probs.size()];
                int index = 0;
                for (Map.Entry<ConqueringAction, Double> entry : probs.entrySet()) {
                    prediction[index] = entry.getValue(); // entryMap is always sorted
                    index++;
                }
                return prediction;
            }

            @Override
            public double[][] apply(ConqueringState[] observationArray) {
                var prediction = new double[observationArray.length][];
                for (int i = 0; i < prediction.length; i++) {
                    prediction[i] = apply(observationArray[i]);
                }
                return prediction;
            }

            @Override
            public List<double[]> apply(List<ConqueringState> stateList) {
                var output = new ArrayList<double[]>(stateList.size());
                for (int i = 0; i < stateList.size(); i++) {
                    output.add(apply(stateList.get(i)));
                }
                return output;
            }
        };
    }

    @Override
    public String readableStringRepresentation() {
        return Arrays.toString(this.playerPositionArray) + " " + Arrays.toString(this.playerPositionArray_hidden);
    }

    @Override
    public List<String> getCsvHeader() {
        return List.of("Nope");
    }

    @Override
    public List<String> getCsvRecord() {
        return List.of("Nope");
    }

    @Override
    public int getInGameEntityIdOnTurn() {
        return entityIdOnTurn;
    }

    @Override
    public boolean isEnvironmentEntityOnTurn() {
        return entityIdOnTurn == ENVIRONMENT_ENTITY_ID;
    }

    @Override
    public boolean isInGame(int inGameEntityId) {
        if(inGameEntityId == 0) {
            return true;
        }
        return isInGameArray[inGameEntityId - 1];
    }

    public boolean[] getIsEntityEliminatedArray() {
        return isEntityEliminatedArray;
    }

    @Override
    public boolean isFinalState() {
        return inGamePlayerCount == 0 || staticPart.getTotalStepsAllowed() <= stepsDone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConqueringState that = (ConqueringState) o;

        if (inGamePlayerCount != that.inGamePlayerCount) return false;
        if (entityIdOnTurn != that.entityIdOnTurn) return false;
        if (playerIdOnTurn != that.playerIdOnTurn) return false;
        if (stepsDone != that.stepsDone) return false;
        if (!staticPart.equals(that.staticPart)) return false;
        if (!Arrays.equals(playerPositionArray, that.playerPositionArray)) return false;
        if (!Arrays.equals(playerPositionArray_hidden, that.playerPositionArray_hidden)) return false;
        return Arrays.equals(isInGameArray, that.isInGameArray);
    }

    @Override
    public int hashCode() {
        int result = staticPart.hashCode();
        result = 31 * result + Arrays.hashCode(playerPositionArray);
        result = 31 * result + Arrays.hashCode(playerPositionArray_hidden);
        result = 31 * result + Arrays.hashCode(isInGameArray);
        result = 31 * result + inGamePlayerCount;
        result = 31 * result + entityIdOnTurn;
        result = 31 * result + playerIdOnTurn;
        result = 31 * result + stepsDone;
        return result;
    }
}
