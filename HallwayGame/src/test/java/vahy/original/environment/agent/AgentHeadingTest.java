package vahy.original.environment.agent;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.original.environment.HallwayAction;

public class AgentHeadingTest {

    @Test
    public void turnRightTest() {
        Assert.assertEquals(AgentHeading.NORTH.turn(HallwayAction.TURN_RIGHT), AgentHeading.EAST);
        Assert.assertEquals(AgentHeading.EAST.turn(HallwayAction.TURN_RIGHT), AgentHeading.SOUTH);
        Assert.assertEquals(AgentHeading.SOUTH.turn(HallwayAction.TURN_RIGHT), AgentHeading.WEST);
        Assert.assertEquals(AgentHeading.WEST.turn(HallwayAction.TURN_RIGHT), AgentHeading.NORTH);
        Assert.assertEquals(AgentHeading.NORTH.turn(HallwayAction.TURN_RIGHT).turn(HallwayAction.TURN_RIGHT).turn(HallwayAction.TURN_RIGHT).turn(HallwayAction.TURN_RIGHT), AgentHeading.NORTH);
    }

    @Test
    public void turnLeftTest() {
        Assert.assertEquals(AgentHeading.NORTH.turn(HallwayAction.TURN_LEFT), AgentHeading.WEST);
        Assert.assertEquals(AgentHeading.EAST.turn(HallwayAction.TURN_LEFT), AgentHeading.NORTH);
        Assert.assertEquals(AgentHeading.SOUTH.turn(HallwayAction.TURN_LEFT), AgentHeading.EAST);
        Assert.assertEquals(AgentHeading.WEST.turn(HallwayAction.TURN_LEFT), AgentHeading.SOUTH);
        Assert.assertEquals(AgentHeading.NORTH.turn(HallwayAction.TURN_LEFT).turn(HallwayAction.TURN_LEFT).turn(HallwayAction.TURN_LEFT).turn(HallwayAction.TURN_LEFT), AgentHeading.NORTH);
    }

}
