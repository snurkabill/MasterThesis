package vahy.examples.simplifiedHallway;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
import vahy.impl.model.ImmutableStateRewardReturn;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ArrayUtils;
import vahy.utils.EnumUtils;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SHState implements State<SHAction, DoubleVector, SHState>, Observation {

    public static final int REWARD_OBSERVATION_SHIFT = 3;
    public static final int ENVIRONMENT_ID = 0;
    public static final int PLAYER_ID = 1;
    public static final SHAction[] ENVIRONMENT_NO_TRAP_ACTION_ARRAY = new SHAction[] {SHAction.NO_ACTION};
    public static final SHAction[] ENVIRONMENT_TRAP_ACTION_ARRAY = new SHAction[] {SHAction.TRAP, SHAction.NO_ACTION};
    public static final SHAction[] PLAYER_ENTITY_ACTION_ARRAY = new SHAction[] {SHAction.UP, SHAction.DOWN, SHAction.RIGHT, SHAction.LEFT};

    private final int agentXCoordination;
    private final int agentYCoordination;
    private final SHStaticPart staticPart;
    private final boolean isAgentTurn;
    private final boolean isAgentKilled;
    private final double[][] rewards;
    private final int rewardsLeft;
    private final double[] doubleObservation;

    private static double getXPortion(SHStaticPart staticPart, int x) {
        int xTotal = staticPart.getWalls().length - 3;
        int xAgentFixed = x - 1;
        return xTotal == 0 ? 0.0 : ((xAgentFixed / (double) xTotal) - 0.5);
    }

    private static double getYPortion(SHStaticPart staticPart, int y) {
        int yTotal = staticPart.getWalls()[0].length - 3;
        int yAgentFixed = y - 1;
        return yTotal == 0 ? 0.0 : ((yAgentFixed / (double) yTotal) - 0.5);
    }

    public SHState(SHStaticPart staticPart, int x, int y, boolean isAgentTurn, boolean isAgentKilled, double[][] rewards, int rewardsLeft) {
        this.agentXCoordination = x;
        this.agentYCoordination = y;
        this.staticPart = staticPart;
        this.isAgentTurn = isAgentTurn;
        this.isAgentKilled = isAgentKilled;
        this.rewards = rewards;
        this.rewardsLeft = rewardsLeft;
        this.doubleObservation = new double[REWARD_OBSERVATION_SHIFT + rewardsLeft];
        this.doubleObservation[0] = isAgentTurn ? 1.0 : 0.0;
        this.doubleObservation[1] = getXPortion(staticPart, x);
        this.doubleObservation[2] = getYPortion(staticPart, y);
        for (int i = REWARD_OBSERVATION_SHIFT; i < doubleObservation.length; i++) {
            doubleObservation[i] = 1.0;
        }
    }

    public SHState(SHStaticPart staticPart, int x, int y, boolean isAgentTurn, boolean isAgentKilled, double[][] rewards, int rewardsLeft, double[] doubleObservation) {
        this.agentXCoordination = x;
        this.agentYCoordination = y;
        this.staticPart = staticPart;
        this.isAgentTurn = isAgentTurn;
        this.isAgentKilled = isAgentKilled;
        this.rewards = rewards;
        this.rewardsLeft = rewardsLeft;
        this.doubleObservation = doubleObservation;
    }

    private EnumMap<SHAction, Double> getEnvironmentProbabilities() {
        var actionMap = new EnumMap<SHAction, Double>(SHAction.class);
        if(isAgentTurn) {
            return actionMap;
        }
        var sum = 0.0;
        var traps = staticPart.getTrapProbabilities();
        if(traps[agentXCoordination][agentYCoordination] != 0) {
            actionMap.put(SHAction.TRAP, traps[agentXCoordination][agentYCoordination]);
            sum += traps[agentXCoordination][agentYCoordination];
        }
        actionMap.put(SHAction.NO_ACTION, 1.0 - sum);
        return actionMap;
    }

    @Override
    public SHAction[] getAllPossibleActions() {
        if(isAgentTurn) {
            return PLAYER_ENTITY_ACTION_ARRAY;
        } else {
            var traps = staticPart.getTrapProbabilities();
            if(traps[agentXCoordination][agentYCoordination] != 0) {
                return ENVIRONMENT_TRAP_ACTION_ARRAY;
            } else {
                return ENVIRONMENT_NO_TRAP_ACTION_ARRAY;
            }
        }
    }

    @Override
    public int getTotalEntityCount() {
        return 2;
    }

    @Override
    public StateRewardReturn<SHAction, DoubleVector, SHState> applyAction(SHAction actionType) {
        if (isFinalState()) {
            throw new IllegalStateException("Cannot apply actions on final state");
        }

        if(isAgentTurn) {
            return applyPlayerAction(actionType);
        } else {
            return applyOpponentAction(actionType);
        }
    }

    @Override
    public DoubleVector getInGameEntityObservation(int inGameEntityId) {
        return new DoubleVector(doubleObservation);
    }

    @Override
    public DoubleVector getCommonObservation(int inGameEntityId) {
        return new DoubleVector(doubleObservation);
    }

    private boolean isMoveDoable(int x, int y) {
        return !staticPart.getWalls()[x][y];
    }

    private ImmutableTuple<Integer, Integer> tryMakeMove(SHAction action) {
        switch (action) {
            case UP:
                return new ImmutableTuple<>(agentXCoordination, agentYCoordination + 1);
            case DOWN:
                return new ImmutableTuple<>(agentXCoordination, agentYCoordination - 1);
            case RIGHT:
                return new ImmutableTuple<>(agentXCoordination - 1, agentYCoordination);
            case LEFT:
                return new ImmutableTuple<>(agentXCoordination + 1, agentYCoordination);
            default:
                throw EnumUtils.createExceptionForNotExpectedEnumValue(action);
        }
    }

    private StateRewardReturn<SHAction, DoubleVector, SHState> applyPlayerAction(SHAction action) {
        var agentCoordinates = tryMakeMove(action);
        var isMoveDoable = isMoveDoable(agentCoordinates.getFirst(), agentCoordinates.getSecond());

        var newObservation = new double[doubleObservation.length];
        System.arraycopy(doubleObservation, 0, newObservation, 0, doubleObservation.length);
        newObservation[0] = 0.0;

        if(isMoveDoable) {
            var newX = agentCoordinates.getFirst();
            var newY = agentCoordinates.getSecond();


            newObservation[1] = getXPortion(staticPart, newX);
            newObservation[2] = getYPortion(staticPart, newY);

            if(rewards[newX][newY] != 0.0) {
                var rewardId = staticPart.getRewardIds()[newX][newY];
                var reward = new double[2];
                reward[PLAYER_ID] = rewards[newX][newY] - staticPart.getDefaultStepPenalty();
                int rewardCount = rewards[newX][newY] != 0.0 ? rewardsLeft - 1 : rewardsLeft;
                var newRewards = ArrayUtils.cloneArray(rewards);
                newRewards[newX][newY] = 0.0;
                newObservation[REWARD_OBSERVATION_SHIFT + rewardId] = -1.0;
                return new ImmutableStateRewardReturn<>(new SHState(staticPart, newX, newY, false, false, newRewards, rewardCount, newObservation), reward);
            } else {
                return new ImmutableStateRewardReturn<>(new SHState(staticPart, newX, newY, false, false, rewards, rewardsLeft, newObservation), staticPart.getNoRewardGained());
            }
        } else {
            return new ImmutableStateRewardReturn<>(new SHState(staticPart, agentXCoordination, agentYCoordination, false, false, rewards, rewardsLeft, newObservation), staticPart.getNoRewardGained());
        }

    }

    private StateRewardReturn<SHAction, DoubleVector, SHState> applyOpponentAction(SHAction action) {
        var newObservation = new double[doubleObservation.length];
        System.arraycopy(doubleObservation, 0, newObservation, 0, doubleObservation.length);
        newObservation[0] = 1.0;
        switch(action) {
            case TRAP:
                return new ImmutableStateRewardReturn<>(new SHState(
                    staticPart,
                    agentXCoordination,
                    agentYCoordination,
                    true,
                    true,
                    rewards,
                    rewardsLeft,
                    newObservation
                    ),
                    staticPart.getEnvironmentMovementReward());
            case NO_ACTION:
                return new ImmutableStateRewardReturn<>(new SHState(
                    staticPart,
                    agentXCoordination,
                    agentYCoordination,
                    true,
                    false,
                    rewards,
                    rewardsLeft,
                    newObservation),
                    staticPart.getEnvironmentMovementReward());
            default:
                throw EnumUtils.createExceptionForNotExpectedEnumValue(action);
        }
    }

    @Override
    public Predictor<SHState> getKnownModelWithPerfectObservationPredictor() {
        return new Predictor<>() {

            @Override
            public double[] apply(SHState observation) {
                var probs = observation.getEnvironmentProbabilities();
                var prediction = new double[probs.size()];
                int index = 0;
                for (Map.Entry<SHAction, Double> entry : probs.entrySet()) {
                    prediction[index] = entry.getValue(); // entryMap is always sorted
                    index++;
                }
                return prediction;
            }

            @Override
            public double[][] apply(SHState[] observationArray) {
                var prediction = new double[observationArray.length][];
                for (int i = 0; i < prediction.length; i++) {
                    prediction[i] = apply(observationArray[i]);
                }
                return prediction;
            }

            @Override
            public List<double[]> apply(List<SHState> shStates) {
                var output = new ArrayList<double[]>(shStates.size());
                for (int i = 0; i < shStates.size(); i++) {
                    output.add(apply(shStates.get(i)));
                }
                return output;
            }
        };
    }

    @Override
    public String readableStringRepresentation() {
        return "TODO: Implement this...";
    }

    @Override
    public List<String> getCsvHeader() {
        return List.of("TODO: Implement this...");
    }

    @Override
    public List<String> getCsvRecord() {
        return List.of("TODO: Implement this...");
    }

    @Override
    public int getInGameEntityIdOnTurn() {
        return isAgentTurn ? PLAYER_ID : ENVIRONMENT_ID;
    }

    @Override
    public boolean isEnvironmentEntityOnTurn() {
        return !isAgentTurn;
    }

    @Override
    public boolean isInGame(int inGameEntityId) {
        if(inGameEntityId == PLAYER_ID && isAgentKilled) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isFinalState() {
        return isAgentKilled || rewardsLeft == 0;
    }

    public boolean isAgentKilled() {
        return isAgentKilled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SHState shState = (SHState) o;

        if (agentXCoordination != shState.agentXCoordination) return false;
        if (agentYCoordination != shState.agentYCoordination) return false;
        if (isAgentTurn != shState.isAgentTurn) return false;
        if (isAgentKilled != shState.isAgentKilled) return false;
        if (rewardsLeft != shState.rewardsLeft) return false;
        return Arrays.deepEquals(rewards, shState.rewards);
    }

    @Override
    public int hashCode() {
        int result = agentXCoordination;
        result = 31 * result + agentYCoordination;
        result = 31 * result + (isAgentTurn ? 1 : 0);
        result = 31 * result + (isAgentKilled ? 1 : 0);
        result = 31 * result + Arrays.deepHashCode(rewards);
        result = 31 * result + rewardsLeft;
        return result;
    }
}
