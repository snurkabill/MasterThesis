package vahy.environment.state;

import vahy.api.model.StateRewardReturn;
import vahy.environment.ActionType;
import vahy.environment.agent.AgentHeading;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.utils.ArrayUtils;
import vahy.utils.EnumUtils;
import vahy.utils.ImmutableTuple;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ImmutableStateImpl implements PaperState<ActionType, DoubleReward, DoubleVector, ImmutableStateImpl> {

    public static final int ADDITIONAL_DIMENSION_AGENT_ON_TRAP = 1;
    public static final int ADDITIONAL_DIMENSION_AGENT_HEADING = 4;

    public static final int AGENT_LOCATION_REPRESENTATION = -3;
    public static final int TRAP_LOCATION_REPRESENTATION = -2;
    public static final int WALL_LOCATION_REPRESENTATION = -1;

    private final StaticGamePart staticGamePart;
    private final double[][] rewards;
    private final int rewardsLeft;

    private final boolean isAgentKilled;
    private final int agentXCoordination;
    private final int agentYCoordination;
    private final AgentHeading agentHeading;
    private final boolean isAgentTurn;
    private final boolean hasAgentMoved;
    private final boolean hasAgentResigned;

    public ImmutableStateImpl(
        StaticGamePart staticGamePart,
        double[][] rewards,
        int agentXCoordination,
        int agentYCoordination,
        AgentHeading agentHeading) {
        this(staticGamePart,
            rewards,
            agentXCoordination,
            agentYCoordination,
            agentHeading,
            true,
            Arrays
                .stream(rewards)
                .map(doubles -> Arrays
                    .stream(doubles)
                    .filter(value -> value > 0.0)
                    .count())
                .mapToInt(Long::intValue)
                .sum(),
            false,
            false,
            false);
    }

    private ImmutableStateImpl(
        StaticGamePart staticGamePart,
        double[][] rewards,
        int agentXCoordination,
        int agentYCoordination,
        AgentHeading agentHeading,
        boolean isAgentTurn,
        int rewardsLeft,
        boolean isAgentKilled,
        boolean hasAgentMoved,
        boolean hasAgentResigned) {
        this.isAgentTurn = isAgentTurn;
        this.staticGamePart = staticGamePart;
        this.rewards = rewards;
        this.agentXCoordination = agentXCoordination;
        this.agentYCoordination = agentYCoordination;
        this.agentHeading = agentHeading;
        this.rewardsLeft = rewardsLeft;
        this.isAgentKilled = isAgentKilled;
        this.hasAgentMoved = hasAgentMoved;
        this.hasAgentResigned = hasAgentResigned;
    }

    public ImmutableTuple<List<ActionType>, List<Double>> environmentActionsWithProbabilities() {
        List<ActionType> possibleActions = new LinkedList<>();
        List<Double> actionProbabilities = new LinkedList<>();
        if(isAgentTurn) {
            return new ImmutableTuple<>(possibleActions, actionProbabilities);
        }
        double sum = 0.0;

        boolean[][] walls = staticGamePart.getWalls();
        double[][] traps = staticGamePart.getTrapProbabilities();

        if(hasAgentMoved) {
            ImmutableTuple<Integer, Integer> coordinates = getRightCoordinates(agentXCoordination, agentYCoordination, agentHeading);
            if(!walls[coordinates.getFirst()][coordinates.getSecond()]) {
                if(traps[coordinates.getFirst()][coordinates.getSecond()] != 0) {
                    possibleActions.add(ActionType.NOISY_RIGHT);
                    double noisyRightProb = staticGamePart.getNoisyMoveProbability() / 2.0 * (1 - traps[coordinates.getFirst()][coordinates.getSecond()]);
                    actionProbabilities.add(noisyRightProb);
                    sum += noisyRightProb;
                    possibleActions.add(ActionType.NOISY_RIGHT_TRAP);
                    double noisyRightTrapProb = staticGamePart.getNoisyMoveProbability() / 2.0 * traps[coordinates.getFirst()][coordinates.getSecond()];
                    actionProbabilities.add(noisyRightTrapProb);
                    sum += noisyRightTrapProb;
                } else {
                    possibleActions.add(ActionType.NOISY_RIGHT);
                    double noisyRightProb = staticGamePart.getNoisyMoveProbability() / 2.0;
                    actionProbabilities.add(noisyRightProb);
                    sum += noisyRightProb;
                }
            }
            coordinates = getLeftCoordinates(agentXCoordination, agentYCoordination, agentHeading);
            if(!walls[coordinates.getFirst()][coordinates.getSecond()]) {
                if(traps[coordinates.getFirst()][coordinates.getSecond()] != 0) {
                    possibleActions.add(ActionType.NOISY_LEFT);
                    double noisyLeftProb = staticGamePart.getNoisyMoveProbability() / 2.0 * (1 - traps[coordinates.getFirst()][coordinates.getSecond()]);
                    actionProbabilities.add(noisyLeftProb);
                    sum += noisyLeftProb;
                    possibleActions.add(ActionType.NOISY_LEFT_TRAP);
                    double noisyLeftTrapProb = staticGamePart.getNoisyMoveProbability() / 2.0 * traps[coordinates.getFirst()][coordinates.getSecond()];
                    actionProbabilities.add(noisyLeftTrapProb);
                    sum += noisyLeftTrapProb;
                } else {
                    possibleActions.add(ActionType.NOISY_LEFT);
                    double noisyLeftProb = staticGamePart.getNoisyMoveProbability() / 2.0;
                    actionProbabilities.add(noisyLeftProb);
                    sum += noisyLeftProb;
                }
            }
            if(traps[agentXCoordination][agentYCoordination] != 0) {
                possibleActions.add(ActionType.TRAP);
                double straightTrapProb = (1 - staticGamePart.getNoisyMoveProbability()) * traps[agentXCoordination][agentYCoordination];
                actionProbabilities.add(straightTrapProb);
                sum += straightTrapProb;
            }
        } else {
            if(traps[agentXCoordination][agentYCoordination] != 0) {
                possibleActions.add(ActionType.TRAP);
                actionProbabilities.add(traps[agentXCoordination][agentYCoordination]);
                sum += traps[agentXCoordination][agentYCoordination];
            }
        }

        if(sum > 1) {
            throw new IllegalStateException("Sum of probabilities should be less than one");
        }
        possibleActions.add(ActionType.NO_ACTION);
        actionProbabilities.add(1.0 - sum);
        return new ImmutableTuple<>(possibleActions, actionProbabilities);
    }

    @Override
    public ActionType[] getAllPossibleActions() {
        if(isAgentTurn) {
            return ActionType.playerActions;
        } else {
            return environmentActionsWithProbabilities().getFirst().toArray(new ActionType[0]);
        }
    }

    @Override
    public StateRewardReturn<ActionType, DoubleReward, DoubleVector, ImmutableStateImpl> applyAction(ActionType actionType) {
        if (isFinalState()) {
            throw new IllegalStateException("Cannot apply actions on final state");
        }
        if(isAgentTurn != actionType.isPlayerAction()) {
            throw new IllegalStateException("Inconsistency between player turn and applying action");
        }
        if(isAgentTurn) {
            switch (actionType) {
                case FORWARD:
                    ImmutableTuple<Integer, Integer> agentCoordinates = makeForwardMove();
                    double reward = rewards[agentCoordinates.getFirst()][agentCoordinates.getSecond()] - staticGamePart.getDefaultStepPenalty();
                    double[][] newRewards = ArrayUtils.cloneArray(rewards);
                    int rewardCount = rewards[agentCoordinates.getFirst()][agentCoordinates.getSecond()] != 0.0 ? rewardsLeft - 1 : rewardsLeft;
                    if (rewards[agentCoordinates.getFirst()][agentCoordinates.getSecond()] != 0.0) {
                        newRewards[agentCoordinates.getFirst()][agentCoordinates.getSecond()] = 0.0;
                    }
                    return new ImmutableStateRewardReturnTuple<>(
                        new ImmutableStateImpl(
                            staticGamePart,
                            newRewards,
                            agentCoordinates.getFirst(),
                            agentCoordinates.getSecond(),
                            agentHeading,
                            false,
                            rewardCount,
                            isAgentKilled,
                            agentCoordinates.getFirst() != agentXCoordination || agentCoordinates.getSecond() != agentYCoordination,
                            false),
                        new DoubleReward(reward));
                case TURN_RIGHT:
                case TURN_LEFT:
                    AgentHeading newAgentHeading = agentHeading.turn(actionType);
                    return new ImmutableStateRewardReturnTuple<>(
                        new ImmutableStateImpl(
                            staticGamePart,
                            ArrayUtils.cloneArray(rewards),
                            agentXCoordination,
                            agentYCoordination,
                            newAgentHeading,
                            false,
                            rewardsLeft,
                            isAgentKilled,
                            false,
                            false),
                        new DoubleReward(-staticGamePart.getDefaultStepPenalty()));
//                case RESIGN:
//                    return new ImmutableStateRewardReturnTuple<>(
//                        new ImmutableStateImpl(
//                            staticGamePart,
//                            ArrayUtils.cloneArray(rewards),
//                            agentXCoordination,
//                            agentYCoordination,
//                            agentHeading,
//                            false,
//                            rewardsLeft,
//                            isAgentKilled,
//                            false,
//                            true),
//                        new DoubleReward(-staticGamePart.getDefaultStepPenalty())
//                        );
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(actionType);
            }
        } else {
            switch (actionType) {
                case NO_ACTION:
                    ImmutableStateImpl state = new ImmutableStateImpl(
                        staticGamePart,
                        ArrayUtils.cloneArray(rewards),
                        agentXCoordination,
                        agentYCoordination,
                        agentHeading,
                        true,
                        rewardsLeft,
                        isAgentKilled,
                        false,
                        false);
                    return new ImmutableStateRewardReturnTuple<>(state, new DoubleReward(0.0));
                case NOISY_RIGHT:
                    ImmutableTuple<Integer, Integer> newRightCoordinates = makeRightMove();
                    return new ImmutableStateRewardReturnTuple<>(new ImmutableStateImpl(
                        staticGamePart,
                        ArrayUtils.cloneArray(rewards),
                        newRightCoordinates.getFirst(),
                        newRightCoordinates.getSecond(),
                        agentHeading,
                        true,
                        rewardsLeft,
                        isAgentKilled,
                        false,
                        false), new DoubleReward(0.0));
                case NOISY_LEFT:
                    ImmutableTuple<Integer, Integer> newLeftCoordinates = makeLeftMove();
                    return new ImmutableStateRewardReturnTuple<>(new ImmutableStateImpl(
                        staticGamePart,
                        ArrayUtils.cloneArray(rewards),
                        newLeftCoordinates.getFirst(),
                        newLeftCoordinates.getSecond(),
                        agentHeading,
                        true,
                        rewardsLeft,
                        isAgentKilled,
                        false,
                        false), new DoubleReward(0.0));
                case TRAP:
                    return new ImmutableStateRewardReturnTuple<>(new ImmutableStateImpl(
                        staticGamePart,
                        ArrayUtils.cloneArray(rewards),
                        agentXCoordination,
                        agentYCoordination,
                        agentHeading,
                        true,
                        rewardsLeft,
                        true,
                        false,
                        false), new DoubleReward(0.0));
                case NOISY_RIGHT_TRAP:
                    ImmutableTuple<Integer, Integer> newRightTrapCoordinates = makeRightMove();
                    return new ImmutableStateRewardReturnTuple<>(new ImmutableStateImpl(
                        staticGamePart,
                        ArrayUtils.cloneArray(rewards),
                        newRightTrapCoordinates.getFirst(),
                        newRightTrapCoordinates.getSecond(),
                        agentHeading,
                        true,
                        rewardsLeft,
                        true,
                        false,
                        false), new DoubleReward(0.0));
                case NOISY_LEFT_TRAP:
                    ImmutableTuple<Integer, Integer> newLeftTrapCoordinates = makeLeftMove();
                    return new ImmutableStateRewardReturnTuple<>(new ImmutableStateImpl(
                        staticGamePart,
                        ArrayUtils.cloneArray(rewards),
                        newLeftTrapCoordinates.getFirst(),
                        newLeftTrapCoordinates.getSecond(),
                        agentHeading,
                        true,
                        rewardsLeft,
                        true,
                        false,
                        false), new DoubleReward(0.0));
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(actionType);
            }
        }
    }

    @Override
    public ImmutableStateImpl deepCopy() {
        return new ImmutableStateImpl(
            staticGamePart,
            ArrayUtils.cloneArray(rewards),
            agentXCoordination,
            agentYCoordination,
            agentHeading,
            isAgentTurn,
            rewardsLeft,
            isAgentKilled,
            hasAgentMoved,
            hasAgentResigned);
    }

    @Override
    public boolean isFinalState() {
        return isAgentKilled || rewardsLeft == 0 || hasAgentResigned;
    }

    public boolean isAgentKilled() {
        return isAgentKilled;
    }

    public boolean isHasAgentResigned() {
        return hasAgentResigned;
    }

    @Override
    public DoubleVector getObservation() {
        switch(staticGamePart.getStateRepresentation()) {
            case FULL:
                return getFullDoubleVectorialObservation();
            case COMPACT:
                return getCompactDOubleVectorialObservation();
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(staticGamePart.getStateRepresentation());
        }
    }

    private DoubleVector getFullDoubleVectorialObservation() {
        boolean[][] walls = staticGamePart.getWalls();
        double[][] trapProbabilities = staticGamePart.getTrapProbabilities();
        double[] vector = new double[walls.length * walls[0].length + ADDITIONAL_DIMENSION_AGENT_ON_TRAP + ADDITIONAL_DIMENSION_AGENT_HEADING];
        int dimension = walls[0].length;
        for (int i = 0; i < walls.length; i++) {
            for (int j = 0; j < dimension; j++) {
                vector[i * dimension + j] = walls[i][j] ?
                    WALL_LOCATION_REPRESENTATION
                    : i == agentXCoordination && j == agentYCoordination ?
                    AGENT_LOCATION_REPRESENTATION
                    : trapProbabilities[i][j] != 0.0 ?
                    TRAP_LOCATION_REPRESENTATION
                    : rewards[i][j];
            }
        }
        int[] headingRepresentationAsArray = agentHeading.getHeadingRepresentationAsArray();
        vector[vector.length - 5] = headingRepresentationAsArray[0];
        vector[vector.length - 4] = headingRepresentationAsArray[1];
        vector[vector.length - 3] = headingRepresentationAsArray[2];
        vector[vector.length - 2] = headingRepresentationAsArray[3];
        vector[vector.length - 1] = isAgentStandingOnTrap() ? 1.0 : 0.0;
        return new DoubleVector(vector);
    }

    public DoubleVector getCompactDOubleVectorialObservation() {
        // experimental shit
        double[] vector = new double[6 + this.staticGamePart.getTotalRewardsCount()];

        int xTotal = this.staticGamePart.getWalls().length - 3;
        int yTotal = this.staticGamePart.getWalls()[0].length - 3;

        int xAgentFixed = agentXCoordination - 1;
        int yAgentFixed = agentYCoordination - 1;

        double xPortion = xAgentFixed / (double) xTotal;
        double yPortion = yAgentFixed / (double) yTotal;

        vector[0] = xPortion;
        vector[1] = yPortion;

        boolean[] rewards = this.staticGamePart.getLeftRewardsAsVector(this.rewards);

        if(rewards.length != this.staticGamePart.getTotalRewardsCount()) {
            throw new IllegalStateException("There is mismatch in dimensions");
        }

        for (int i = 0; i < rewards.length; i++) {
            vector[2 + i] = rewards[i] ? 1.0 : 0.0;
        }

        int[] headingRepresentationAsArray = agentHeading.getHeadingRepresentationAsArray();
        vector[vector.length - 4] = headingRepresentationAsArray[0];
        vector[vector.length - 3] = headingRepresentationAsArray[1];
        vector[vector.length - 2] = headingRepresentationAsArray[2];
        vector[vector.length - 1] = headingRepresentationAsArray[3];

        return new DoubleVector(vector);
    }

    @Override
    public String readableStringRepresentation() {
        boolean[][] walls = staticGamePart.getWalls();
        double[][] trapProbabilities = staticGamePart.getTrapProbabilities();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < walls.length; i++) {
            for (int j = 0; j < walls[0].length; j++) {
                builder.append(walls[i][j]
                    ? "W "
                    : i == agentXCoordination && j == agentYCoordination
                        ? "A "
                        : trapProbabilities[i][j] != 0.0
                            ? "X "
                            // : String.valueOf(rewards[i][j]));
                            : rewards[i][j] == 0
                                ? "  "
                                : "G ");
            }
            builder.append(System.lineSeparator());
        }
        builder.append(isAgentStandingOnTrap() ? "T " : "N ");
        builder.append(agentHeading.getHeadingReadableRepresentation());
        return builder.toString();
    }

    @Override
    public boolean isOpponentTurn() {
        return !isAgentTurn();
    }

    private ImmutableTuple<Integer, Integer> getForwardCoordinates(int x, int y, AgentHeading agentHeading) {
        switch (agentHeading) {
            case NORTH:
                x = x - 1;
                break;
            case SOUTH:
                x = x + 1;
                break;
            case EAST:
                y = y - 1;
                break;
            case WEST:
                y = y + 1;
                break;
            default:
                throw new IllegalArgumentException("Unknown enum value: [" + agentHeading + "]");
        }
        return new ImmutableTuple<>(x, y);
    }

    private ImmutableTuple<Integer, Integer> getRightCoordinates(int x, int y, AgentHeading agentHeading) {
        switch (agentHeading) {
            case NORTH:
                y = y - 1;
                break;
            case SOUTH:
                y = y + 1;
                break;
            case EAST:
                x = x + 1;
                break;
            case WEST:
                x = x - 1;
                break;
            default:
                throw new IllegalArgumentException("Unknown enum value: [" + agentHeading + "]");
        }
        return new ImmutableTuple<>(x, y);
    }

    private ImmutableTuple<Integer, Integer> getLeftCoordinates(int x, int y, AgentHeading agentHeading) {
        switch (agentHeading) {
            case NORTH:
                y = y + 1;
                break;
            case SOUTH:
                y = y - 1;
                break;
            case EAST:
                x = x - 1;
                break;
            case WEST:
                x = x + 1;
                break;
            default:
                throw new IllegalArgumentException("Unknown enum value: [" + agentHeading + "]");
        }
        return new ImmutableTuple<>(x, y);
    }

    private ImmutableTuple<Integer, Integer> makeForwardMove() {
        return checkMoveValidity(getForwardCoordinates(agentXCoordination, agentYCoordination, agentHeading));
    }

    private ImmutableTuple<Integer, Integer> makeRightMove() {
        return checkMoveValidity(getRightCoordinates(agentXCoordination, agentYCoordination, agentHeading));
    }

    private ImmutableTuple<Integer, Integer> makeLeftMove() {
        return checkMoveValidity(getLeftCoordinates(agentXCoordination, agentYCoordination, agentHeading));
    }

    private ImmutableTuple<Integer, Integer> checkMoveValidity(ImmutableTuple<Integer, Integer> coordinates) {
        if (coordinates.getFirst() < 0) {
            throw new IllegalStateException("Agent's coordinate X cannot be negative");
        }
        if (coordinates.getFirst() >= staticGamePart.getWalls().length) {
            throw new IllegalStateException("Agent's coordinate X cannot be bigger or equal to maze size");
        }
        if (coordinates.getSecond() < 0) {
            throw new IllegalStateException("Agent's coordinate Y cannot be negative");
        }
        if (coordinates.getSecond() >= staticGamePart.getWalls()[agentXCoordination].length) {
            throw new IllegalStateException("Agent's coordinate Y cannot be bigger or equal to maze size");
        }

        if (!staticGamePart.getWalls()[coordinates.getFirst()][coordinates.getSecond()]) {
            return coordinates;
        } else {
            return new ImmutableTuple<>(agentXCoordination, agentYCoordination);
        }
    }

    public boolean isAgentStandingOnTrap() {
        return staticGamePart.getTrapProbabilities()[agentXCoordination][agentYCoordination] != 0.0;
    }

    public boolean isAgentTurn() {
        return isAgentTurn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableStateImpl)) return false;

        ImmutableStateImpl that = (ImmutableStateImpl) o;

        if (rewardsLeft != that.rewardsLeft) return false;
        if (isAgentKilled != that.isAgentKilled) return false;
        if (agentXCoordination != that.agentXCoordination) return false;
        if (agentYCoordination != that.agentYCoordination) return false;
        if (isAgentTurn() != that.isAgentTurn()) return false;
        if (hasAgentMoved != that.hasAgentMoved) return false;
        if (!staticGamePart.equals(that.staticGamePart)) return false;
        if (!Arrays.deepEquals(rewards, that.rewards)) return false;
        if (agentHeading != that.agentHeading) return false;
        return hasAgentResigned == that.hasAgentResigned;
    }

    @Override
    public int hashCode() {
        int result = staticGamePart.hashCode();
        result = 31 * result + Arrays.deepHashCode(rewards);
        result = 31 * result + rewardsLeft;
        result = 31 * result + (isAgentKilled ? 1 : 0);
        result = 31 * result + agentXCoordination;
        result = 31 * result + agentYCoordination;
        result = 31 * result + agentHeading.hashCode();
        result = 31 * result + (isAgentTurn() ? 1 : 0);
        result = 31 * result + (hasAgentMoved ? 1 : 0);
        return result;
    }

    @Override
    public boolean isRiskHit() {
        return isAgentKilled();
    }
}
