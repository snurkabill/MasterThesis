package vahy.environment.state;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.environment.ActionType;
import vahy.environment.agent.AgentHeading;
import vahy.impl.model.DoubleScalarReward;
import vahy.impl.model.DoubleVectorialObservation;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.utils.ArrayUtils;
import vahy.utils.EnumUtils;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ImmutableStateImpl implements State<ActionType, DoubleScalarReward, DoubleVectorialObservation> {

    public static final int ADDITIONAL_DIMENSION_AGENT_ON_TRAP = 1;
    public static final int ADDITIONAL_DIMENSION_AGENT_HEADING = 1;

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
    private final ActionType previousAction;

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
            ActionType.NO_ACTION);
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
        ActionType previousAction) {
        this.isAgentTurn = isAgentTurn;
        this.staticGamePart = staticGamePart;
        this.rewards = rewards;
        this.agentXCoordination = agentXCoordination;
        this.agentYCoordination = agentYCoordination;
        this.agentHeading = agentHeading;
        this.rewardsLeft = rewardsLeft;
        this.isAgentKilled = isAgentKilled;
        this.previousAction = previousAction;
    }

    private ImmutableTuple<List<ActionType>, List<Double>> environmentActionsWithProbabilities() {
        List<ActionType> possibleActions = new LinkedList<>();
        List<Double> actionProbabilities = new LinkedList<>();
        double sum = 0.0;

        boolean[][] walls = staticGamePart.getWalls();
        double[][] traps = staticGamePart.getTrapProbabilities();
        if(traps[agentXCoordination][agentYCoordination] != 0) {
            possibleActions.add(ActionType.TRAP);
            actionProbabilities.add(traps[agentXCoordination][agentYCoordination]);
            sum += traps[agentXCoordination][agentYCoordination];
        }

        if(previousAction == ActionType.FORWARD) {
            ImmutableTuple<Integer, Integer> coordinates = getRightCoordinates(agentXCoordination, agentYCoordination, agentHeading);
            if(!walls[coordinates.getFirst()][coordinates.getSecond()]) {
                possibleActions.add(ActionType.NOISY_RIGHT);
                actionProbabilities.add(staticGamePart.getNoisyMoveProbability() / 2.0);
                sum += staticGamePart.getNoisyMoveProbability() / 2.0;
                if(traps[coordinates.getFirst()][coordinates.getSecond()] != 0) {
                    possibleActions.add(ActionType.NOISY_RIGHT_TRAP);
                    actionProbabilities.add((staticGamePart.getNoisyMoveProbability() / 2.0) * traps[coordinates.getFirst()][coordinates.getSecond()]);
                    sum += (staticGamePart.getNoisyMoveProbability() / 2.0) * traps[coordinates.getFirst()][coordinates.getSecond()];
                }
            }

            coordinates = getLeftCoordinates(agentXCoordination, agentYCoordination, agentHeading);
            if(!walls[coordinates.getFirst()][coordinates.getSecond()]) {
                possibleActions.add(ActionType.NOISY_LEFT);
                actionProbabilities.add(staticGamePart.getNoisyMoveProbability() / 2.0);
                sum += staticGamePart.getNoisyMoveProbability() / 2.0;
                if(traps[coordinates.getFirst()][coordinates.getSecond()] != 0) {
                    possibleActions.add(ActionType.NOISY_LEFT_TRAP);
                    actionProbabilities.add((staticGamePart.getNoisyMoveProbability() / 2.0) * traps[coordinates.getFirst()][coordinates.getSecond()]);
                    sum += (staticGamePart.getNoisyMoveProbability() / 2.0) * traps[coordinates.getFirst()][coordinates.getSecond()];
                }
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

    public StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> applyEnvironmentAction() {
        if(isAgentTurn) {
            throw new IllegalStateException("Environment action cannot be applied when it is agent's turn");
        }
        ImmutableTuple<List<ActionType>, List<Double>> actions = environmentActionsWithProbabilities();
        ActionType pickedAction = actions.getFirst().get(RandomDistributionUtils.getRandomIndexFromDistribution(actions.getSecond(), staticGamePart.getRandom()));
        return applyAction(pickedAction);
    }


    @Override
    public StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> applyAction(ActionType actionType) {
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
                    return new ImmutableStateRewardReturnTuple<>(new ImmutableStateImpl(
                        staticGamePart,
                        newRewards,
                        agentCoordinates.getFirst(),
                        agentCoordinates.getSecond(),
                        agentHeading,
                        false,
                        rewardCount,
                        isAgentKilled,
                        actionType), new DoubleScalarReward(reward));
                case TURN_RIGHT:
                case TURN_LEFT:
                    AgentHeading newAgentHeading = agentHeading.turn(actionType);
                    return new ImmutableStateRewardReturnTuple<>(new ImmutableStateImpl(
                            staticGamePart,
                            ArrayUtils.cloneArray(rewards),
                            agentXCoordination,
                            agentYCoordination,
                            newAgentHeading,
                            false,
                            rewardsLeft,
                            isAgentKilled,
                        actionType),
                        new DoubleScalarReward(-staticGamePart.getDefaultStepPenalty()));
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(actionType);
            }
        } else {
            switch (actionType) {
                case NO_ACTION:
                    State<ActionType, DoubleScalarReward, DoubleVectorialObservation> state = new ImmutableStateImpl(
                        staticGamePart,
                        ArrayUtils.cloneArray(rewards),
                        agentXCoordination,
                        agentYCoordination,
                        agentHeading,
                        true,
                        rewardsLeft,
                        isAgentKilled,
                        actionType);
                    return new ImmutableStateRewardReturnTuple<>(state, new DoubleScalarReward(0.0));
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
                        actionType), new DoubleScalarReward(0.0));
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
                        actionType), new DoubleScalarReward(0.0));
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
                        actionType), new DoubleScalarReward(0.0));
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
                        actionType), new DoubleScalarReward(0.0));
                case NOISY_LEFT_TRAP:
                    ImmutableTuple<Integer, Integer> newLeftTrapCoordinates = makeRightMove();
                    return new ImmutableStateRewardReturnTuple<>(new ImmutableStateImpl(
                        staticGamePart,
                        ArrayUtils.cloneArray(rewards),
                        newLeftTrapCoordinates.getFirst(),
                        newLeftTrapCoordinates.getSecond(),
                        agentHeading,
                        true,
                        rewardsLeft,
                        true,
                        actionType), new DoubleScalarReward(0.0));
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(actionType);
            }
        }
    }

    @Override
    public State<ActionType, DoubleScalarReward, DoubleVectorialObservation> deepCopy() {
        return new ImmutableStateImpl(
            staticGamePart,
            ArrayUtils.cloneArray(rewards),
            agentXCoordination,
            agentYCoordination,
            agentHeading,
            isAgentTurn,
            rewardsLeft,
            isAgentKilled,
            previousAction);
    }

    @Override
    public boolean isFinalState() {
        return isAgentKilled || rewardsLeft == 0;
    }


    @Override
    public DoubleVectorialObservation getObservation() {
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
        vector[vector.length - 1] = isAgentStandingOnTrap() ? 1.0 : 0.0;
        vector[vector.length - 2] = agentHeading.getHeadingRepresentation();
        return new DoubleVectorialObservation(vector);
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


    private boolean isAgentStandingOnTrap() {
        return staticGamePart.getTrapProbabilities()[agentXCoordination][agentYCoordination] != 0.0;
    }


    public boolean isAgentTurn() {
        return isAgentTurn;
    }
}
