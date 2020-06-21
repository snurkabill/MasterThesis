package vahy.original.environment.agent;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.original.environment.HallwayAction;

public class AgentHeadingTest {

    @Test
    public void turnRightTest() {
        assertEquals(AgentHeading.NORTH.turn(HallwayAction.TURN_RIGHT), AgentHeading.EAST);
        assertEquals(AgentHeading.EAST.turn(HallwayAction.TURN_RIGHT), AgentHeading.SOUTH);
        assertEquals(AgentHeading.SOUTH.turn(HallwayAction.TURN_RIGHT), AgentHeading.WEST);
        assertEquals(AgentHeading.WEST.turn(HallwayAction.TURN_RIGHT), AgentHeading.NORTH);
        assertEquals(AgentHeading.NORTH.turn(HallwayAction.TURN_RIGHT).turn(HallwayAction.TURN_RIGHT).turn(HallwayAction.TURN_RIGHT).turn(HallwayAction.TURN_RIGHT), AgentHeading.NORTH);
    }

    @Test
    public void turnLeftTest() {
        assertEquals(AgentHeading.NORTH.turn(HallwayAction.TURN_LEFT), AgentHeading.WEST);
        assertEquals(AgentHeading.EAST.turn(HallwayAction.TURN_LEFT), AgentHeading.NORTH);
        assertEquals(AgentHeading.SOUTH.turn(HallwayAction.TURN_LEFT), AgentHeading.EAST);
        assertEquals(AgentHeading.WEST.turn(HallwayAction.TURN_LEFT), AgentHeading.SOUTH);
        assertEquals(AgentHeading.NORTH.turn(HallwayAction.TURN_LEFT).turn(HallwayAction.TURN_LEFT).turn(HallwayAction.TURN_LEFT).turn(HallwayAction.TURN_LEFT), AgentHeading.NORTH);
    }

}
