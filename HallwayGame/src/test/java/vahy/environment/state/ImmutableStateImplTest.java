package vahy.environment.state;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.environment.ActionType;
import vahy.environment.agent.AgentHeading;
import vahy.utils.ArrayUtils;
import vahy.utils.EnumUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;

public class ImmutableStateImplTest {

    private static final double DOUBLE_TOLERANCE = Math.pow(10, -10);

    private ImmutableStateImpl getHallGame1() {
        boolean[][] walls = new boolean[][]{
            {true, true, true},
            {true, false, true},
            {true, false, true},
            {true, false, true},
            {true, false, true},
            {true, false, true},
            {true, true, true}
        };
        double [][] rewards = new double[][] {
            {0.0, 0.0, 0.0},
            {0.0, 100.0, 0.0},
            {0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0},
        };
        StaticGamePart staticGamePart = new StaticGamePart(new SplittableRandom(0), StateRepresentation.FULL, new double[walls.length][walls[0].length], ArrayUtils.cloneArray(rewards), walls, -1, 0.5, 1);
        return new ImmutableStateImpl(staticGamePart, rewards, 5, 1, AgentHeading.NORTH);
    }

    private ImmutableStateImpl getHallGame2() {
        boolean[][] walls = new boolean[][]{
            {true, true,  true,  true, true},
            {true, false, false, false, true},
            {true, false, false, false, true},
            {true, false, false, false, true},
            {true, false, false, false, true},
            {true, false, false, false, true},
            {true, true,  true,  true, true}
        };
        double[][] rewards = new double[][] {
            {0.0, 0.0, 0.0, 0.0,   0.0},
            {0.0, 0.0, 0.0, 100.0, 0.0},
            {0.0, 0.0, 0.0, 0.0,   0.0},
            {0.0, 0.0, 0.0, 0.0,   0.0},
            {0.0, 0.0, 0.0, 0.0,   0.0},
            {0.0, 0.0, 0.0, 0.0,   0.0},
            {0.0, 0.0, 0.0, 0.0,   0.0},
        };
        double[][] traps = new double[][] {
            {0.0, 0.0, 0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0, 0.0, 0.0},
            {0.0, 0.2, 0.2, 0.2, 0.0},
            {0.0, 0.1, 0.0, 0.5, 0.0},
            {0.0, 0.0, 0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0, 0.0, 0.0},
        };
        StaticGamePart staticGamePart = new StaticGamePart(new SplittableRandom(0), StateRepresentation.FULL, traps, ArrayUtils.cloneArray(rewards), walls, -1, 0.5, 1);
        return new ImmutableStateImpl(staticGamePart, rewards, 5, 2, AgentHeading.NORTH);
    }

    private void assertAgentCoordinations(ImmutableStateImpl game, int expectedX, int expectedY, int gameXdim) {
        double[] observation = game.getObservation().getObservedVector();
        Assert.assertEquals(observation[gameXdim * expectedX + expectedY], ImmutableStateImpl.AGENT_LOCATION_REPRESENTATION, DOUBLE_TOLERANCE);
    }

    private void assertAgentHeading(ImmutableStateImpl game, AgentHeading expectedAgentHeading) {
        double[] observation = game.getObservation().getObservedVector();
        int observationIndex = agentHeadingIndexOnArray(expectedAgentHeading);
        Assert.assertEquals(observation[observation.length + observationIndex], 1.0, DOUBLE_TOLERANCE);
    }

    private int agentHeadingIndexOnArray(AgentHeading expectedHeading) {
        switch(expectedHeading) {
            case NORTH:
                return -5;
            case EAST:
                return -4;
            case SOUTH:
                return -3;
            case WEST:
                return -2;
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(expectedHeading);
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void envActionWhenAgentOnTurnTest() {
        ImmutableStateImpl game = getHallGame1();
        game.applyAction(ActionType.TRAP);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void agentActionWhenEnvOnTurnTest() {
        ImmutableStateImpl game = getHallGame1();
        game.applyAction(ActionType.FORWARD).getState().applyAction(ActionType.FORWARD);
    }

    @Test
    public void simpleForwardStateChangeTest() {
        ImmutableStateImpl state1 = getHallGame1();
        assertAgentCoordinations(state1, 5, 1, 3);
        assertAgentHeading(state1, AgentHeading.NORTH);
        ImmutableStateImpl state2 = (ImmutableStateImpl) state1.applyAction(ActionType.FORWARD).getState();
        Assert.assertFalse(state2.isAgentTurn());
        Assert.assertFalse(state2.isFinalState());
        assertAgentCoordinations(state2, 4, 1, 3);
        assertAgentHeading(state1, AgentHeading.NORTH);
    }

    @Test
    public void toWallForwardStateChangeTest() {
        ImmutableStateImpl state1 = getHallGame1();
        assertAgentCoordinations(state1, 5, 1, 3);
        assertAgentHeading(state1, AgentHeading.NORTH);
        ImmutableStateImpl state2 = (ImmutableStateImpl) state1
            .applyAction(ActionType.TURN_LEFT).getState()
            .applyAction(ActionType.NO_ACTION).getState()
            .applyAction(ActionType.FORWARD).getState();
        Assert.assertFalse(state2.isAgentTurn());
        Assert.assertFalse(state2.isFinalState());
        assertAgentCoordinations(state2, 5, 1, 3);
    }

    @Test
    public void agentRightTurnTest() {
        ImmutableStateImpl state1 = getHallGame1();
        assertAgentHeading(state1, AgentHeading.NORTH);
        ImmutableStateImpl state2 = (ImmutableStateImpl) state1
            .applyAction(ActionType.TURN_RIGHT).getState();
        Assert.assertFalse(state2.isAgentTurn());
        Assert.assertFalse(state2.isFinalState());
        assertAgentHeading(state2, AgentHeading.EAST);
    }

    @Test
    public void agentLeftTurnTest() {
        ImmutableStateImpl state1 = getHallGame1();
        assertAgentHeading(state1, AgentHeading.NORTH);
        ImmutableStateImpl state2 = (ImmutableStateImpl) state1
            .applyAction(ActionType.TURN_LEFT).getState();
        Assert.assertFalse(state2.isAgentTurn());
        Assert.assertFalse(state2.isFinalState());
        assertAgentHeading(state2, AgentHeading.WEST);
    }

    @Test
    public void emptyEnvironmentActionsWithProbabilitiesTest() {
        ImmutableStateImpl state = getHallGame1();
        Assert.assertTrue(state.isAgentTurn());
        Assert.assertTrue(state.environmentActionsWithProbabilities().getFirst().isEmpty());
        Assert.assertTrue(state.environmentActionsWithProbabilities().getSecond().isEmpty());
    }

    @Test
    public void onlyNoActionEnvironmentActionsWithProbabilitiesTest() {
        ImmutableStateImpl state1 = getHallGame1();
        Assert.assertTrue(state1.isAgentTurn());
        ImmutableStateImpl state2 = (ImmutableStateImpl) state1
            .applyAction(ActionType.FORWARD).getState();
        Assert.assertEquals(state2.environmentActionsWithProbabilities().getFirst(), Collections.singletonList(ActionType.NO_ACTION));
        Assert.assertEquals(state2.environmentActionsWithProbabilities().getSecond(), Collections.singletonList(1.0));

        ImmutableStateImpl state3 = (ImmutableStateImpl) state2
            .applyAction(ActionType.NO_ACTION).getState()
            .applyAction(ActionType.TURN_LEFT).getState()
            .applyAction(ActionType.NO_ACTION).getState()
            .applyAction(ActionType.FORWARD).getState();
        Assert.assertEquals(state3.environmentActionsWithProbabilities().getFirst(), Collections.singletonList(ActionType.NO_ACTION));
        Assert.assertEquals(state3.environmentActionsWithProbabilities().getSecond(), Collections.singletonList(1.0));
    }

    @Test
    public void noisyMovesEnvironmentActionsWithProbabilitiesTest() {
        ImmutableStateImpl state1 = getHallGame2();
        Assert.assertTrue(state1.isAgentTurn());
        ImmutableStateImpl state2 = (ImmutableStateImpl) state1
            .applyAction(ActionType.FORWARD).getState();
        List<ActionType> moves = state2.environmentActionsWithProbabilities().getFirst();
        List<ActionType> expectedMoves = Arrays.asList(ActionType.NOISY_RIGHT, ActionType.NOISY_LEFT, ActionType.NO_ACTION);
        Assert.assertEquals(moves, expectedMoves);
        Assert.assertEquals(state2.environmentActionsWithProbabilities().getSecond(), Arrays.asList(0.25, 0.25, 0.5));
    }

    @Test
    public void noisyTrapMovesEnvironmentActionsWithProbabilitiesTest() {
        ImmutableStateImpl state1 = getHallGame2();
        Assert.assertTrue(state1.isAgentTurn());
        ImmutableStateImpl state2 = (ImmutableStateImpl) state1
            .applyAction(ActionType.FORWARD).getState()
            .applyAction(ActionType.NO_ACTION).getState()
            .applyAction(ActionType.FORWARD).getState();
        List<ActionType> moves = state2.environmentActionsWithProbabilities().getFirst();
        List<ActionType> expectedMoves = Arrays.asList(ActionType.NOISY_RIGHT, ActionType.NOISY_RIGHT_TRAP, ActionType.NOISY_LEFT, ActionType.NOISY_LEFT_TRAP, ActionType.NO_ACTION);
        Assert.assertEquals(moves, expectedMoves);
        Assert.assertEquals(state2.environmentActionsWithProbabilities().getSecond(), Arrays.asList(0.25 * 0.9, 0.25 * 0.1, 0.25 * 0.5, 0.25 * 0.5, 0.5));
    }

    @Test
    public void noisyTrapMovesWithTrapEnvironmentActionsWithProbabilitiesTest() {
        ImmutableStateImpl state1 = getHallGame2();
        Assert.assertTrue(state1.isAgentTurn());
        ImmutableStateImpl state2 = (ImmutableStateImpl) state1
            .applyAction(ActionType.FORWARD).getState()
            .applyAction(ActionType.NO_ACTION).getState()
            .applyAction(ActionType.FORWARD).getState()
            .applyAction(ActionType.NO_ACTION).getState()
            .applyAction(ActionType.FORWARD).getState();
        List<ActionType> moves = state2.environmentActionsWithProbabilities().getFirst();
        List<ActionType> expectedMoves = Arrays.asList(ActionType.NOISY_RIGHT, ActionType.NOISY_RIGHT_TRAP, ActionType.NOISY_LEFT, ActionType.NOISY_LEFT_TRAP, ActionType.TRAP, ActionType.NO_ACTION);
        Assert.assertEquals(moves, expectedMoves);
        Assert.assertEquals(state2.environmentActionsWithProbabilities().getSecond(), Arrays.asList(0.25 * 0.8, 0.25 * 0.2, 0.25 * 0.8, 0.25 * 0.2, 0.5 * 0.2, 0.5 * 0.8));
    }

}
