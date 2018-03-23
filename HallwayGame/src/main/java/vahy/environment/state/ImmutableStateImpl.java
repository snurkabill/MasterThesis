package vahy.environment.state;

import vahy.environment.ActionType;
import vahy.environment.agent.AgentHeading;
import vahy.utils.ArrayUtils;
import vahy.utils.ImmutableTuple;

import java.util.Arrays;
import java.util.Random;

public class ImmutableStateImpl implements IState {

    private final Random random;
    private final double[][] rewards;
    private final double[][] trapProbabilities;
    private final boolean[][] walls;
    private final int rewardsLeft;

    private final boolean isAgentKilled;
    private final int agentXCoordination;
    private final int agentYCoordination;
    private final AgentHeading agentHeading;

    private final double defaultStepPenalty;

    private ImmutableStateImpl(
            double[][] rewards,
            double[][] trapProbabilities,
            boolean[][] walls,
            int agentXCoordination,
            int agentYCoordination,
            AgentHeading agentHeading,
            double defaultStepPenalty,
            int rewardsLeft,
            Random random) {
        this.walls = walls;
        this.trapProbabilities = trapProbabilities;
        this.rewards = rewards;
        this.agentXCoordination = agentXCoordination;
        this.agentYCoordination = agentYCoordination;
        this.agentHeading = agentHeading;
        this.defaultStepPenalty = defaultStepPenalty;
        this.random = random;
        this.rewardsLeft = rewardsLeft;
        this.isAgentKilled = isAgentKilled(trapProbabilities[agentXCoordination][agentYCoordination], random);
    }

    private boolean isAgentKilled(double trapProbability, Random random) {
        return random.nextDouble() < trapProbability;
    }

    public ImmutableStateImpl(
            double[][] rewards,
            double[][] trapProbabilities,
            boolean[][] walls,
            int agentXCoordination,
            int agentYCoordination,
            AgentHeading agentHeading,
            double defaultStepPenalty,
            Random random) {
        this(rewards,
                trapProbabilities,
                walls,
                agentXCoordination,
                agentYCoordination,
                agentHeading,
                defaultStepPenalty,
                Arrays
                    .stream(rewards)
                    .map(doubles -> Arrays
                            .stream(doubles)
                            .filter(value -> value > 0.0)
                            .count())
                    .mapToInt(Long::intValue)
                    .sum(),
                random);
    }

    @Override
    public ActionType[] getListOfPossibleActions() {
        if(isFinalState()) {
            return new ActionType[0];
        } else {
            return ActionType.values();
        }
    }

    @Override
    public RewardStateReturn applyAction(ActionType actionType) {
        if(isFinalState()) {
            throw new IllegalStateException("Cannot apply actions on final state");
        }
        if(actionType == ActionType.FORWARD) {
            ImmutableTuple<Integer, Integer> agentCoordinates = makeForwardAction();
            double reward = rewards[agentCoordinates.getFirst()][agentCoordinates.getSecond()] - defaultStepPenalty;
            double[][] newRewards = ArrayUtils.cloneArray(rewards);
            if(rewards[agentCoordinates.getFirst()][agentCoordinates.getSecond()] != 0.0) {
                newRewards[agentCoordinates.getFirst()][agentCoordinates.getSecond()] = 0.0;
            }
            IState state = new ImmutableStateImpl(
                    newRewards,
                    ArrayUtils.cloneArray(trapProbabilities),
                    ArrayUtils.cloneArray(walls),
                    agentCoordinates.getFirst(),
                    agentCoordinates.getSecond(),
                    agentHeading,
                    defaultStepPenalty,
                    rewardsLeft,
                    random);
            return new RewardStateReturn(reward, state);
        } else {
            IState state = new ImmutableStateImpl(
                    ArrayUtils.cloneArray(rewards),
                    ArrayUtils.cloneArray(trapProbabilities),
                    ArrayUtils.cloneArray(walls),
                    agentXCoordination,
                    agentYCoordination,
                    agentHeading.turn(actionType),
                    defaultStepPenalty,
                    rewardsLeft,
                    random);
            return new RewardStateReturn(-defaultStepPenalty, state);
        }
    }

    @Override
    public IState deepCopy() {
        return new ImmutableStateImpl(
                ArrayUtils.cloneArray(rewards),
                ArrayUtils.cloneArray(trapProbabilities),
                ArrayUtils.cloneArray(walls),
                agentXCoordination,
                agentYCoordination,
                agentHeading,
                defaultStepPenalty,
                rewardsLeft,
                random);
    }

    private ImmutableTuple<Integer, Integer> makeForwardAction() {
        int x = agentXCoordination;
        int y = agentYCoordination;
        switch (agentHeading) {
            case NORTH:
                x = agentXCoordination - 1;
                if(x < 0) {
                    throw new IllegalStateException("Agent's coordinate X cannot be negative");
                }
                break;
            case SOUTH:
                x = agentXCoordination + 1;
                if(x >= walls.length) {
                    throw new IllegalStateException("Agent's coordinate X cannot be bigger or equal to maze size");
                }
                break;
            case EAST:
                y = agentYCoordination - 1;
                if(y < 0) {
                    throw new IllegalStateException("Agent's coordinate Y cannot be negative");
                }
                break;
            case WEST:
                y = agentYCoordination + 1;
                if(y >= walls[agentXCoordination].length) {
                    throw new IllegalStateException("Agent's coordinate Y cannot be bigger or equal to maze size");
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown enum value: [" + agentHeading + "]");
        }
        if(!walls[x][y]) {
            return new ImmutableTuple<>(x, y);
        } else {
            return new ImmutableTuple<>(agentXCoordination, agentYCoordination);
        }
    }

    @Override
    public boolean isFinalState() {
        return isAgentKilled || rewardsLeft == 0;
    }

}
