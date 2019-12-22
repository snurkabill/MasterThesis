package vahy.original.environment.state;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.original.environment.HallwayAction;
import vahy.original.environment.agent.AgentHeading;
import vahy.utils.ArrayUtils;
import vahy.utils.EnumUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HallwayStateTest {

    private static final double DOUBLE_TOLERANCE = Math.pow(10, -10);

    private HallwayStateImpl getHallGame1() {
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
        StaticGamePart staticGamePart = new StaticGamePart(StateRepresentation.FULL, new double[walls.length][walls[0].length], ArrayUtils.cloneArray(rewards), walls, -1, 0.5, 1);
        return new HallwayStateImpl(staticGamePart, rewards, 5, 1, AgentHeading.NORTH);
    }

    private HallwayStateImpl getHallGame2() {
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
        StaticGamePart staticGamePart = new StaticGamePart(StateRepresentation.FULL, traps, ArrayUtils.cloneArray(rewards), walls, -1, 0.5, 1);
        return new HallwayStateImpl(staticGamePart, rewards, 5, 2, AgentHeading.NORTH);
    }

    private HallwayStateImpl getHallGame3() {
        boolean[][] walls = new boolean[][]{
            {true, true,  true,  true, true},
            {true, false, false, false, true},
            {true, false, false, false, true},
            {true, true,  true,  true, true}
        };
        double[][] rewards = new double[][] {
            {0.0, 0.0, 0.0, 0.0,   0.0},
            {0.0, 100.0, 100.0, 100.0, 0.0},
            {0.0, 0.0, 0.0, 0.0,   0.0},
            {0.0, 0.0, 0.0, 0.0,   0.0},
        };
        double[][] traps = new double[][] {
            {0.0, 0.0, 0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0, 0.0, 0.0},
        };
        StaticGamePart staticGamePart = new StaticGamePart(StateRepresentation.FULL, traps, ArrayUtils.cloneArray(rewards), walls, 1, 0.5, 3);
        return new HallwayStateImpl(staticGamePart, rewards, 2, 2, AgentHeading.NORTH);
    }

    private void assertAgentCoordinations(HallwayStateImpl game, int expectedX, int expectedY, int gameXdim) {
        double[] observation = game.getPlayerObservation().getObservedVector();
        Assert.assertEquals(observation[gameXdim * expectedX + expectedY], HallwayStateImpl.AGENT_LOCATION_REPRESENTATION, DOUBLE_TOLERANCE);
    }

    private void assertAgentHeading(HallwayStateImpl game, AgentHeading expectedAgentHeading) {
        double[] observation = game.getPlayerObservation().getObservedVector();
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
        HallwayStateImpl game = getHallGame1();
        game.applyAction(HallwayAction.TRAP);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void agentActionWhenEnvOnTurnTest() {
        HallwayStateImpl game = getHallGame1();
        game.applyAction(HallwayAction.FORWARD).getState().applyAction(HallwayAction.FORWARD);
    }

    @Test
    public void simpleForwardStateChangeTest() {
        HallwayStateImpl state1 = getHallGame1();
        assertAgentCoordinations(state1, 5, 1, 3);
        assertAgentHeading(state1, AgentHeading.NORTH);
        HallwayStateImpl state2 = state1.applyAction(HallwayAction.FORWARD).getState();
        Assert.assertFalse(state2.isAgentTurn());
        Assert.assertFalse(state2.isFinalState());
        assertAgentCoordinations(state2, 4, 1, 3);
        assertAgentHeading(state1, AgentHeading.NORTH);
    }

    @Test
    public void toWallForwardStateChangeTest() {
        HallwayStateImpl state1 = getHallGame1();
        assertAgentCoordinations(state1, 5, 1, 3);
        assertAgentHeading(state1, AgentHeading.NORTH);
        HallwayStateImpl state2 = state1
            .applyAction(HallwayAction.TURN_LEFT).getState()
            .applyAction(HallwayAction.NO_ACTION).getState()
            .applyAction(HallwayAction.FORWARD).getState();
        Assert.assertFalse(state2.isAgentTurn());
        Assert.assertFalse(state2.isFinalState());
        assertAgentCoordinations(state2, 5, 1, 3);
    }

    @Test
    public void agentRightTurnTest() {
        HallwayStateImpl state1 = getHallGame1();
        assertAgentHeading(state1, AgentHeading.NORTH);
        HallwayStateImpl state2 = state1
            .applyAction(HallwayAction.TURN_RIGHT).getState();
        Assert.assertFalse(state2.isAgentTurn());
        Assert.assertFalse(state2.isFinalState());
        assertAgentHeading(state2, AgentHeading.EAST);
    }

    @Test
    public void agentLeftTurnTest() {
        HallwayStateImpl state1 = getHallGame1();
        assertAgentHeading(state1, AgentHeading.NORTH);
        HallwayStateImpl state2 = state1
            .applyAction(HallwayAction.TURN_LEFT).getState();
        Assert.assertFalse(state2.isAgentTurn());
        Assert.assertFalse(state2.isFinalState());
        assertAgentHeading(state2, AgentHeading.WEST);
    }

    @Test
    public void emptyEnvironmentActionsWithProbabilitiesTest() {
        HallwayStateImpl state = getHallGame1();
        Assert.assertTrue(state.isAgentTurn());
        Assert.assertTrue(state.getOpponentObservation().getProbabilities().getFirst().isEmpty());
        Assert.assertTrue(state.getOpponentObservation().getProbabilities().getSecond().isEmpty());
    }

    @Test
    public void onlyNoActionEnvironmentActionsWithProbabilitiesTest() {
        HallwayStateImpl state1 = getHallGame1();
        Assert.assertTrue(state1.isAgentTurn());
        HallwayStateImpl state2 = state1
            .applyAction(HallwayAction.FORWARD).getState();
        Assert.assertEquals(state2.getOpponentObservation().getProbabilities().getFirst(), Collections.singletonList(HallwayAction.NO_ACTION));
        Assert.assertEquals(state2.getOpponentObservation().getProbabilities().getSecond(), Collections.singletonList(1.0));

        HallwayStateImpl state3 = state2
            .applyAction(HallwayAction.NO_ACTION).getState()
            .applyAction(HallwayAction.TURN_LEFT).getState()
            .applyAction(HallwayAction.NO_ACTION).getState()
            .applyAction(HallwayAction.FORWARD).getState();
        Assert.assertEquals(state3.getOpponentObservation().getProbabilities().getFirst(), Collections.singletonList(HallwayAction.NO_ACTION));
        Assert.assertEquals(state3.getOpponentObservation().getProbabilities().getSecond(), Collections.singletonList(1.0));
    }

    @Test
    public void noisyMovesEnvironmentActionsWithProbabilitiesTest() {
        HallwayStateImpl state1 = getHallGame2();
        Assert.assertTrue(state1.isAgentTurn());
        HallwayStateImpl state2 = state1
            .applyAction(HallwayAction.FORWARD).getState();
        List<HallwayAction> moves = state2.getOpponentObservation().getProbabilities().getFirst();
        List<HallwayAction> expectedMoves = Arrays.asList(HallwayAction.NOISY_RIGHT, HallwayAction.NOISY_LEFT, HallwayAction.NO_ACTION);
        Assert.assertEquals(moves, expectedMoves);
        Assert.assertEquals(state2.getOpponentObservation().getProbabilities().getSecond(), Arrays.asList(0.25, 0.25, 0.5));
    }

    @Test
    public void noisyTrapMovesEnvironmentActionsWithProbabilitiesTest() {
        HallwayStateImpl state1 = getHallGame2();
        Assert.assertTrue(state1.isAgentTurn());
        HallwayStateImpl state2 = state1
            .applyAction(HallwayAction.FORWARD).getState()
            .applyAction(HallwayAction.NO_ACTION).getState()
            .applyAction(HallwayAction.FORWARD).getState();
        List<HallwayAction> moves = state2.getOpponentObservation().getProbabilities().getFirst();
        List<HallwayAction> expectedMoves = Arrays.asList(HallwayAction.NOISY_RIGHT, HallwayAction.NOISY_RIGHT_TRAP, HallwayAction.NOISY_LEFT, HallwayAction.NOISY_LEFT_TRAP, HallwayAction.NO_ACTION);
        Assert.assertEquals(moves, expectedMoves);
        Assert.assertEquals(state2.getOpponentObservation().getProbabilities().getSecond(), Arrays.asList(0.25 * 0.9, 0.25 * 0.1, 0.25 * 0.5, 0.25 * 0.5, 0.5));
    }

    @Test
    public void noisyTrapMovesWithTrapEnvironmentActionsWithProbabilitiesTest() {
        HallwayStateImpl state1 = getHallGame2();
        Assert.assertTrue(state1.isAgentTurn());
        HallwayStateImpl state2 = state1
            .applyAction(HallwayAction.FORWARD).getState()
            .applyAction(HallwayAction.NO_ACTION).getState()
            .applyAction(HallwayAction.FORWARD).getState()
            .applyAction(HallwayAction.NO_ACTION).getState()
            .applyAction(HallwayAction.FORWARD).getState();
        List<HallwayAction> moves = state2.getOpponentObservation().getProbabilities().getFirst();
        List<HallwayAction> expectedMoves = Arrays.asList(HallwayAction.NOISY_RIGHT, HallwayAction.NOISY_RIGHT_TRAP, HallwayAction.NOISY_LEFT, HallwayAction.NOISY_LEFT_TRAP, HallwayAction.TRAP, HallwayAction.NO_ACTION);
        Assert.assertEquals(moves, expectedMoves);
        Assert.assertEquals(state2.getOpponentObservation().getProbabilities().getSecond(), Arrays.asList(0.25 * 0.8, 0.25 * 0.2, 0.25 * 0.8, 0.25 * 0.2, 0.5 * 0.2, 0.5 * 0.8));
    }


    @Test
    public void noisyMoveToGold() {
        HallwayStateImpl state1 = getHallGame3();
        Assert.assertTrue(state1.isAgentTurn());
        var stateRewardReturn = state1.applyAction(HallwayAction.FORWARD);
        Assert.assertTrue(Math.abs(stateRewardReturn.getReward() - 99.0) < DOUBLE_TOLERANCE);
        var stateRewardReturn2 = stateRewardReturn.getState().applyAction(HallwayAction.NOISY_LEFT);
        Assert.assertEquals(stateRewardReturn2.getReward(), 0.0);
        var stateRewardReturn3 = stateRewardReturn2.getState().applyAction(HallwayAction.TURN_RIGHT);
        Assert.assertTrue(Math.abs(stateRewardReturn3.getReward() + 1.0) < DOUBLE_TOLERANCE);
        var stateRewardReturn4 = stateRewardReturn3.getState().applyAction(HallwayAction.NO_ACTION);
        Assert.assertEquals(stateRewardReturn4.getReward(), 0.0);
        var stateRewardReturn5 = stateRewardReturn4.getState().applyAction(HallwayAction.FORWARD);
        Assert.assertTrue(Math.abs(stateRewardReturn5.getReward() + 1.0) < DOUBLE_TOLERANCE);
        var stateRewardReturn6 = stateRewardReturn5.getState().applyAction(HallwayAction.NO_ACTION);
        Assert.assertEquals(stateRewardReturn6.getReward(), 0.0);
        var stateRewardReturn7 = stateRewardReturn6.getState().applyAction(HallwayAction.FORWARD);
        Assert.assertTrue(Math.abs(stateRewardReturn7.getReward() - 99.0) < DOUBLE_TOLERANCE);
        var stateRewardReturn8 = stateRewardReturn7.getState().applyAction(HallwayAction.NO_ACTION);
        Assert.assertEquals(stateRewardReturn8.getReward(), 0.0);
        var stateRewardReturn9 = stateRewardReturn8.getState().applyAction(HallwayAction.TURN_RIGHT);
        Assert.assertTrue(Math.abs(stateRewardReturn9.getReward() + 1.0) < DOUBLE_TOLERANCE);
        var stateRewardReturn10 = stateRewardReturn9.getState().applyAction(HallwayAction.NO_ACTION);
        Assert.assertEquals(stateRewardReturn10.getReward(), 0.0);
        var stateRewardReturn11 = stateRewardReturn10.getState().applyAction(HallwayAction.TURN_RIGHT);
        Assert.assertTrue(Math.abs(stateRewardReturn11.getReward() + 1.0) < DOUBLE_TOLERANCE);
        var stateRewardReturn12 = stateRewardReturn11.getState().applyAction(HallwayAction.NO_ACTION);
        Assert.assertEquals(stateRewardReturn12.getReward(), 0.0);

        var stateRewardReturn13 = stateRewardReturn12.getState().applyAction(HallwayAction.FORWARD);
        Assert.assertTrue(Math.abs(stateRewardReturn13.getReward() + 1.0) < DOUBLE_TOLERANCE);
        var stateRewardReturn14 = stateRewardReturn13.getState().applyAction(HallwayAction.NO_ACTION);
        Assert.assertEquals(stateRewardReturn14.getReward(), 0.0);

        var stateRewardReturn15 = stateRewardReturn14.getState().applyAction(HallwayAction.FORWARD);
        Assert.assertTrue(Math.abs(stateRewardReturn15.getReward() - 99.0) < DOUBLE_TOLERANCE);
    }
}
