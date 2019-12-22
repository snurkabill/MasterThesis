package vahy.resignation.environment.agent;

import vahy.resignation.environment.HallwayActionWithResign;

public enum AgentHeadingWithResign {
    NORTH,
    SOUTH,
    EAST,
    WEST;

    private int headingRepresentation;
    private int[] headingRepresentationAsArray;
    private String readableHeadingRepresentation;
    private AgentHeadingWithResign right;
    private AgentHeadingWithResign left;

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

        NORTH.headingRepresentationAsArray = new int[] {1, 0, 0, 0};
        EAST.headingRepresentationAsArray = new int[] {0, 1, 0, 0};
        SOUTH.headingRepresentationAsArray = new int[] {0, 0, 1, 0};
        WEST.headingRepresentationAsArray = new int[] {0, 0, 0, 1};

        NORTH.readableHeadingRepresentation = "↑";
        EAST.readableHeadingRepresentation = "←";
        SOUTH.readableHeadingRepresentation = "↓";
        WEST.readableHeadingRepresentation = "→";
    }

    public AgentHeadingWithResign turn(HallwayActionWithResign hallwayAction) {
        if(hallwayAction == HallwayActionWithResign.TURN_RIGHT) {
            return right;
        } else if(hallwayAction == HallwayActionWithResign.TURN_LEFT) {
            return left;
        } else {
            throw new IllegalArgumentException("HallwayAction: [" + hallwayAction + "] cannot be used for turning agent");
        }
    }

    public int getHeadingRepresentation() {
        return headingRepresentation;
    }

    public int[] getHeadingRepresentationAsArray() {
        return headingRepresentationAsArray;
    }

    public String getHeadingReadableRepresentation() {
        return readableHeadingRepresentation;
    }
}
