package vahy.environment.state;

import vahy.environment.ActionType;
import vahy.environment.agent.AgentHeading;
import vahy.utils.ArrayUtils;
import vahy.utils.ImmutableTuple;

import java.util.Arrays;
import java.util.Random;

public class ImmutableStateImpl implements IState {

    private final StaticStatePart staticStatePart;
    private final double[][] rewards;
    private final int rewardsLeft;

    private final boolean isAgentKilled;
    private final int agentXCoordination;
    private final int agentYCoordination;
    private final AgentHeading agentHeading;

    public ImmutableStateImpl(
            StaticStatePart staticStatePart,
            double[][] rewards,
            int agentXCoordination,
            int agentYCoordination,
            AgentHeading agentHeading) {
        this(staticStatePart,
                rewards,
                agentXCoordination,
                agentYCoordination,
                agentHeading,
                Arrays
                        .stream(rewards)
                        .map(doubles -> Arrays
                                .stream(doubles)
                                .filter(value -> value > 0.0)
                                .count())
                        .mapToInt(Long::intValue)
                        .sum());
    }

    private ImmutableStateImpl(
            StaticStatePart staticStatePart,
            double[][] rewards,
            int agentXCoordination,
            int agentYCoordination,
            AgentHeading agentHeading,
            int rewardsLeft) {
        this.staticStatePart = staticStatePart;
        this.rewards = rewards;
        this.agentXCoordination = agentXCoordination;
        this.agentYCoordination = agentYCoordination;
        this.agentHeading = agentHeading;
        this.rewardsLeft = rewardsLeft;
        this.isAgentKilled = isAgentKilled(staticStatePart.getTrapProbabilities()[agentXCoordination][agentYCoordination], staticStatePart.getRandom());
    }

    private boolean isAgentKilled(double trapProbability, Random random) {
        return random.nextDouble() < trapProbability;
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
            double reward = rewards[agentCoordinates.getFirst()][agentCoordinates.getSecond()] - staticStatePart.getDefaultStepPenalty();
            double[][] newRewards = ArrayUtils.cloneArray(rewards);
            if(rewards[agentCoordinates.getFirst()][agentCoordinates.getSecond()] != 0.0) {
                newRewards[agentCoordinates.getFirst()][agentCoordinates.getSecond()] = 0.0;
            }
            IState state = new ImmutableStateImpl(
                    staticStatePart,
                    newRewards,
                    agentCoordinates.getFirst(),
                    agentCoordinates.getSecond(),
                    agentHeading,
                    rewardsLeft);
            return new RewardStateReturn(reward, state);
        } else {
            IState state = new ImmutableStateImpl(
                    staticStatePart,
                    ArrayUtils.cloneArray(rewards),
                    agentXCoordination,
                    agentYCoordination,
                    agentHeading.turn(actionType),
                    rewardsLeft);
            return new RewardStateReturn(-staticStatePart.getDefaultStepPenalty(), state);
        }
    }

    @Override
    public IState deepCopy() {
        return new ImmutableStateImpl(
                staticStatePart,
                ArrayUtils.cloneArray(rewards),
                agentXCoordination,
                agentYCoordination,
                agentHeading,
                rewardsLeft);
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
                if(x >= staticStatePart.getWalls().length) {
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
                if(y >= staticStatePart.getWalls()[agentXCoordination].length) {
                    throw new IllegalStateException("Agent's coordinate Y cannot be bigger or equal to maze size");
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown enum value: [" + agentHeading + "]");
        }
        if(!staticStatePart.getWalls()[x][y]) {
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
