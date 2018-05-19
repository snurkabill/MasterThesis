package vahy.environment.agent;

import vahy.environment.ActionType;

public enum AgentHeading {
    NORTH,
    SOUTH,
    EAST,
    WEST;

    private int headingRepresentation;
    private String readableHeadingRepresentation;
    private AgentHeading right;
    private AgentHeading left;

    static {
        NORTH.right = EAST;
        NORTH.left = WEST;
        SOUTH.right = WEST;
        SOUTH.left = EAST;
        EAST.right = SOUTH;
        EAST.left = NORTH;
        WEST.right = NORTH;
        WEST.left = SOUTH;

        NORTH.headingRepresentation = 0;
        EAST.headingRepresentation = 1;
        SOUTH.headingRepresentation = 2;
        WEST.headingRepresentation = 3;

        NORTH.readableHeadingRepresentation = "↑";
        EAST.readableHeadingRepresentation = "←";
        SOUTH.readableHeadingRepresentation = "↓";
        WEST.readableHeadingRepresentation = "→";
    }

    public AgentHeading turn(ActionType actionType) {
        if(actionType == ActionType.TURN_RIGHT) {
            return right;
        } else if(actionType == ActionType.TURN_LEFT) {
            return left;
        } else {
            throw new IllegalArgumentException("ActionType: [" + actionType + "] cannot be used for turning agent");
        }
    }

    public int getHeadingRepresentation() {
        return headingRepresentation;
    }

    public String getHeadingReadableRepresentation() {
        return readableHeadingRepresentation;
    }
}
