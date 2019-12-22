package vahy.resignation.environment.state;

import vahy.api.model.StateRewardReturn;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVector;
import vahy.original.environment.state.StaticGamePart;
import vahy.paperGenerics.PaperState;
import vahy.resignation.environment.HallwayActionWithResign;
import vahy.resignation.environment.agent.AgentHeadingWithResign;
import vahy.utils.ArrayUtils;
import vahy.utils.EnumUtils;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class HallwayStateWithResign implements PaperState<HallwayActionWithResign, DoubleVector, EnvironmentProbabilities, HallwayStateWithResign> {

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
    private final AgentHeadingWithResign agentHeading;
    private final boolean isAgentTurn;
    private final boolean hasAgentMoved;
    private final boolean hasAgentResigned;

    public HallwayStateWithResign(
        StaticGamePart staticGamePart,
        double[][] rewards,
        int agentXCoordination,
        int agentYCoordination,
        AgentHeadingWithResign agentHeading) {
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

    private HallwayStateWithResign(
        StaticGamePart staticGamePart,
        double[][] rewards,
        int agentXCoordination,
        int agentYCoordination,
        AgentHeadingWithResign agentHeading,
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

    private ImmutableTuple<List<HallwayActionWithResign>, List<Double>> environmentActionsWithProbabilities() {
        List<HallwayActionWithResign> possibleActions = new LinkedList<>();
        List<Double> actionProbabilities = new LinkedList<>();
        if(isAgentTurn) {
            return new ImmutableTuple<>(possibleActions, actionProbabilities);
        }
        double sum = 0.0;

        boolean[][] walls = staticGamePart.getWalls();
        double[][] traps = staticGamePart.getTrapProbabilities();

        if(hasAgentMoved) {
            double failedNoisyMoveProbability = 0.0;
            ImmutableTuple<Integer, Integer> coordinates = getRightCoordinates(agentXCoordination, agentYCoordination, agentHeading);
            if(!walls[coordinates.getFirst()][coordinates.getSecond()]) {
                if(traps[coordinates.getFirst()][coordinates.getSecond()] != 0) {
                    possibleActions.add(HallwayActionWithResign.NOISY_RIGHT);
                    double noisyRightProb = staticGamePart.getNoisyMoveProbability() / 2.0 * (1 - traps[coordinates.getFirst()][coordinates.getSecond()]);
                    actionProbabilities.add(noisyRightProb);
                    sum += noisyRightProb;
                    possibleActions.add(HallwayActionWithResign.NOISY_RIGHT_TRAP);
                    double noisyRightTrapProb = staticGamePart.getNoisyMoveProbability() / 2.0 * traps[coordinates.getFirst()][coordinates.getSecond()];
                    actionProbabilities.add(noisyRightTrapProb);
                    sum += noisyRightTrapProb;
                } else {
                    possibleActions.add(HallwayActionWithResign.NOISY_RIGHT);
                    double noisyRightProb = staticGamePart.getNoisyMoveProbability() / 2.0;
                    actionProbabilities.add(noisyRightProb);
                    sum += noisyRightProb;
                }
            } else {
                failedNoisyMoveProbability += staticGamePart.getNoisyMoveProbability() / 2.0;
            }
            coordinates = getLeftCoordinates(agentXCoordination, agentYCoordination, agentHeading);
            if(!walls[coordinates.getFirst()][coordinates.getSecond()]) {
                if(traps[coordinates.getFirst()][coordinates.getSecond()] != 0) {
                    possibleActions.add(HallwayActionWithResign.NOISY_LEFT);
                    double noisyLeftProb = staticGamePart.getNoisyMoveProbability() / 2.0 * (1 - traps[coordinates.getFirst()][coordinates.getSecond()]);
                    actionProbabilities.add(noisyLeftProb);
                    sum += noisyLeftProb;
                    possibleActions.add(HallwayActionWithResign.NOISY_LEFT_TRAP);
                    double noisyLeftTrapProb = staticGamePart.getNoisyMoveProbability() / 2.0 * traps[coordinates.getFirst()][coordinates.getSecond()];
                    actionProbabilities.add(noisyLeftTrapProb);
                    sum += noisyLeftTrapProb;
                } else {
                    possibleActions.add(HallwayActionWithResign.NOISY_LEFT);
                    double noisyLeftProb = staticGamePart.getNoisyMoveProbability() / 2.0;
                    actionProbabilities.add(noisyLeftProb);
                    sum += noisyLeftProb;
                }
            } else {
                failedNoisyMoveProbability += staticGamePart.getNoisyMoveProbability() / 2.0;
            }
            if(traps[agentXCoordination][agentYCoordination] != 0) {
                possibleActions.add(HallwayActionWithResign.TRAP);
                double straightTrapProb = (1 - staticGamePart.getNoisyMoveProbability() + failedNoisyMoveProbability) * traps[agentXCoordination][agentYCoordination];
                actionProbabilities.add(straightTrapProb);
                sum += straightTrapProb;
            }
        } else {
            if(traps[agentXCoordination][agentYCoordination] != 0) {
                possibleActions.add(HallwayActionWithResign.TRAP);
                actionProbabilities.add(traps[agentXCoordination][agentYCoordination]);
                sum += traps[agentXCoordination][agentYCoordination];
            }
        }

        if(sum > 1) {
            throw new IllegalStateException("Sum of probabilities should be less than one");
        }
        possibleActions.add(HallwayActionWithResign.NO_ACTION);
        actionProbabilities.add(1.0 - sum);
        return new ImmutableTuple<>(possibleActions, actionProbabilities);
    }

    @Override
    public HallwayActionWithResign[] getAllPossibleActions() {
        if(isAgentTurn) {
            return HallwayActionWithResign.playerActions;
        } else {
            return environmentActionsWithProbabilities().getFirst().toArray(new HallwayActionWithResign[0]);
        }
    }

    @Override
    public HallwayActionWithResign[] getPossiblePlayerActions() {
        if(isAgentTurn) {
            return HallwayActionWithResign.playerActions;
        } else {
            return new HallwayActionWithResign[0];
        }
    }

    @Override
    public HallwayActionWithResign[] getPossibleOpponentActions() {
        if(isOpponentTurn()) {
            return HallwayActionWithResign.playerActions;
        } else {
            return new HallwayActionWithResign[0];
        }
    }

    @Override
    public StateRewardReturn<HallwayActionWithResign, DoubleVector, EnvironmentProbabilities, HallwayStateWithResign> applyAction(HallwayActionWithResign hallwayActionWithResign) {
        if (isFinalState()) {
            throw new IllegalStateException("Cannot apply actions on final state");
        }
        if(isAgentTurn != hallwayActionWithResign.isPlayerAction()) {
            throw new IllegalStateException("Inconsistency between player turn and applying action");
        }
        if(isAgentTurn) {
            switch (hallwayActionWithResign) {
                case FORWARD:
                    ImmutableTuple<Integer, Integer> agentCoordinates = makeForwardMove();
                    double reward = rewards[agentCoordinates.getFirst()][agentCoordinates.getSecond()] - staticGamePart.getDefaultStepPenalty();
                    double[][] newRewards = ArrayUtils.cloneArray(rewards);
                    int rewardCount = rewards[agentCoordinates.getFirst()][agentCoordinates.getSecond()] != 0.0 ? rewardsLeft - 1 : rewardsLeft;
                    if (rewards[agentCoordinates.getFirst()][agentCoordinates.getSecond()] != 0.0) {
                        newRewards[agentCoordinates.getFirst()][agentCoordinates.getSecond()] = 0.0;
                    }
                    return new ImmutableStateRewardReturnTuple<>(
                        new HallwayStateWithResign(
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
                        reward);
                case TURN_RIGHT:
                case TURN_LEFT:
                    AgentHeadingWithResign newAgentHeadingWithResign = agentHeading.turn(hallwayActionWithResign);
                    return new ImmutableStateRewardReturnTuple<>(
                        new HallwayStateWithResign(
                            staticGamePart,
                            ArrayUtils.cloneArray(rewards),
                            agentXCoordination,
                            agentYCoordination,
                            newAgentHeadingWithResign,
                            false,
                            rewardsLeft,
                            isAgentKilled,
                            false,
                            false),
                        -staticGamePart.getDefaultStepPenalty());
                case RESIGN:
                    return new ImmutableStateRewardReturnTuple<>(
                        new HallwayStateWithResign(
                            staticGamePart,
                            ArrayUtils.cloneArray(rewards),
                            agentXCoordination,
                            agentYCoordination,
                            agentHeading,
                            false,
                            rewardsLeft,
                            isAgentKilled,
                            false,
                            true),
                        -staticGamePart.getDefaultStepPenalty());
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(hallwayActionWithResign);
            }
        } else {
            switch (hallwayActionWithResign) {
                case NO_ACTION:
                    HallwayStateWithResign state = new HallwayStateWithResign(
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
                    return new ImmutableStateRewardReturnTuple<>(state, 0.0);
                case NOISY_RIGHT:
                    ImmutableTuple<Integer, Integer> newRightCoordinates = makeRightMove();
                    return new ImmutableStateRewardReturnTuple<>(new HallwayStateWithResign(
                        staticGamePart,
                        ArrayUtils.cloneArray(rewards),
                        newRightCoordinates.getFirst(),
                        newRightCoordinates.getSecond(),
                        agentHeading,
                        true,
                        rewardsLeft,
                        isAgentKilled,
                        false,
                        false), 0.0);
                case NOISY_LEFT:
                    ImmutableTuple<Integer, Integer> newLeftCoordinates = makeLeftMove();
                    return new ImmutableStateRewardReturnTuple<>(new HallwayStateWithResign(
                        staticGamePart,
                        ArrayUtils.cloneArray(rewards),
                        newLeftCoordinates.getFirst(),
                        newLeftCoordinates.getSecond(),
                        agentHeading,
                        true,
                        rewardsLeft,
                        isAgentKilled,
                        false,
                        false), 0.0);
                case TRAP:
                    return new ImmutableStateRewardReturnTuple<>(new HallwayStateWithResign(
                        staticGamePart,
                        ArrayUtils.cloneArray(rewards),
                        agentXCoordination,
                        agentYCoordination,
                        agentHeading,
                        true,
                        rewardsLeft,
                        true,
                        false,
                        false), 0.0);
                case NOISY_RIGHT_TRAP:
                    ImmutableTuple<Integer, Integer> newRightTrapCoordinates = makeRightMove();
                    return new ImmutableStateRewardReturnTuple<>(new HallwayStateWithResign(
                        staticGamePart,
                        ArrayUtils.cloneArray(rewards),
                        newRightTrapCoordinates.getFirst(),
                        newRightTrapCoordinates.getSecond(),
                        agentHeading,
                        true,
                        rewardsLeft,
                        true,
                        false,
                        false), 0.0);
                case NOISY_LEFT_TRAP:
                    ImmutableTuple<Integer, Integer> newLeftTrapCoordinates = makeLeftMove();
                    return new ImmutableStateRewardReturnTuple<>(new HallwayStateWithResign(
                        staticGamePart,
                        ArrayUtils.cloneArray(rewards),
                        newLeftTrapCoordinates.getFirst(),
                        newLeftTrapCoordinates.getSecond(),
                        agentHeading,
                        true,
                        rewardsLeft,
                        true,
                        false,
                        false), 0.0);
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(hallwayActionWithResign);
            }
        }
    }

    @Override
    public HallwayStateWithResign deepCopy() {
        return new HallwayStateWithResign(
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
    public DoubleVector getPlayerObservation() {
        switch(staticGamePart.getStateRepresentation()) {
            case FULL:
                return getFullDoubleVectorialObservation();
            case COMPACT:
                return getCompactDoubleVectorialObservation();
            case COMPACT_WITH_LOCAL:
                return getCompactWithLocalDoubleVectorialObservation();
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(staticGamePart.getStateRepresentation());
        }
    }

    @Override
    public EnvironmentProbabilities getOpponentObservation() {
        return new EnvironmentProbabilities(this.environmentActionsWithProbabilities());
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

    public DoubleVector getCompactDoubleVectorialObservation() {
        // experimental shit
        double[] vector = new double[6 + this.staticGamePart.getTotalRewardsCount()];

        int xTotal = this.staticGamePart.getWalls().length - 3;
        int yTotal = this.staticGamePart.getWalls()[0].length - 3;

        int xAgentFixed = agentXCoordination - 1;
        int yAgentFixed = agentYCoordination - 1;

        double xPortion = xTotal == 0 ? 0.0 : ((xAgentFixed / (double) xTotal) - 0.5);
        double yPortion = yTotal == 0 ? 0.0 : ((yAgentFixed / (double) yTotal) - 0.5);

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

    public DoubleVector getCompactWithLocalDoubleVectorialObservation() {
        DoubleVector compact = getCompactDoubleVectorialObservation();
        double[][] trapProbabilities = staticGamePart.getTrapProbabilities();
        var compactLength = compact.getObservedVector().length;

        int localSize = 8;
        double[] output = new double[compactLength + localSize];

        System.arraycopy(compact.getObservedVector(), 0, output, 0, compactLength);

        boolean[][] walls = staticGamePart.getWalls();


        int counter = 0;
        for (int x = this.agentXCoordination - 1; x <= this.agentXCoordination + 1; x++) {
            for (int y = this.agentYCoordination - 1; y <= this.agentYCoordination + 1; y++) {

                if(x != this.agentXCoordination || y != this.agentYCoordination) {
                    output[compactLength + counter] = walls[x][y] ?
                        WALL_LOCATION_REPRESENTATION
                        : trapProbabilities[x][y] != 0.0 ?
                        TRAP_LOCATION_REPRESENTATION
                        : rewards[x][y];
                    counter++;
                }
            }
        }


        return new DoubleVector(output);
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
    public List<String> getCsvHeader() {

//        private final double[][] rewards;
//        private final int rewardsLeft;


        var list = new ArrayList<String>();
        list.add("Agent coordinate X");
        list.add("Agent coordinate Y");
        list.add("Is agent killed");
        list.add("Agent heading");
        list.add("Is agent turn");
        list.add("Has agent moved");
        var rewardVector = staticGamePart.getLeftRewardsAsVector(this.rewards);
        for (int i = 0; i < rewardVector.length; i++) {
            list.add("Reward_" + i);
        }
        return list;
    }

    @Override
    public List<String> getCsvRecord() {
        var list = new ArrayList<String>();
        list.add(String.valueOf(agentXCoordination));
        list.add(String.valueOf(agentYCoordination));
        list.add(String.valueOf(isAgentKilled));
        list.add(agentHeading.name());
        list.add(String.valueOf(isAgentTurn));
        list.add(String.valueOf(hasAgentMoved));

        var rewardVector = staticGamePart.getLeftRewardsAsVector(this.rewards);
        for (int i = 0; i < rewardVector.length; i++) {
            list.add(String.valueOf(rewardVector[i]));
        }
        return list;
    }

    @Override
    public boolean isOpponentTurn() {
        return !isAgentTurn();
    }

    private ImmutableTuple<Integer, Integer> getForwardCoordinates(int x, int y, AgentHeadingWithResign agentHeading) {
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

    private ImmutableTuple<Integer, Integer> getRightCoordinates(int x, int y, AgentHeadingWithResign agentHeading) {
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

    private ImmutableTuple<Integer, Integer> getLeftCoordinates(int x, int y, AgentHeadingWithResign agentHeading) {
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
        if (!(o instanceof HallwayStateWithResign)) return false;

        HallwayStateWithResign that = (HallwayStateWithResign) o;

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
