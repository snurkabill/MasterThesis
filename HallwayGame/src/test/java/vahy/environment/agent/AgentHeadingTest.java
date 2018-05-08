package vahy.environment.agent;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.environment.ActionType;

public class AgentHeadingTest {

    @Test
    public void turnRightTest() {
        Assert.assertEquals(AgentHeading.NORTH.turn(ActionType.TURN_RIGHT), AgentHeading.EAST);
        Assert.assertEquals(AgentHeading.EAST.turn(ActionType.TURN_RIGHT), AgentHeading.SOUTH);
        Assert.assertEquals(AgentHeading.SOUTH.turn(ActionType.TURN_RIGHT), AgentHeading.WEST);
        Assert.assertEquals(AgentHeading.WEST.turn(ActionType.TURN_RIGHT), AgentHeading.NORTH);
        Assert.assertEquals(AgentHeading.NORTH.turn(ActionType.TURN_RIGHT).turn(ActionType.TURN_RIGHT).turn(ActionType.TURN_RIGHT).turn(ActionType.TURN_RIGHT), AgentHeading.NORTH);
    }

    @Test
    public void turnLeftTest() {
        Assert.assertEquals(AgentHeading.NORTH.turn(ActionType.TURN_LEFT), AgentHeading.WEST);
        Assert.assertEquals(AgentHeading.EAST.turn(ActionType.TURN_LEFT), AgentHeading.NORTH);
        Assert.assertEquals(AgentHeading.SOUTH.turn(ActionType.TURN_LEFT), AgentHeading.EAST);
        Assert.assertEquals(AgentHeading.WEST.turn(ActionType.TURN_LEFT), AgentHeading.SOUTH);
        Assert.assertEquals(AgentHeading.NORTH.turn(ActionType.TURN_LEFT).turn(ActionType.TURN_LEFT).turn(ActionType.TURN_LEFT).turn(ActionType.TURN_LEFT), AgentHeading.NORTH);
    }

}
