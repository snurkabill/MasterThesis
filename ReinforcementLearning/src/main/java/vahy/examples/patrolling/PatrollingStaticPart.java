package vahy.examples.patrolling;

public class PatrollingStaticPart {


    private final boolean[][] graphMatrix;
    private final int attackLength;
    private final boolean[] isTargetNode;

    public PatrollingStaticPart(boolean[][] graphMatrix, int attackLength, boolean[] isTargetNode) {
        this.graphMatrix = graphMatrix;
        this.attackLength = attackLength;
        this.isTargetNode = isTargetNode;
    }

    public boolean[][] getGraphMatrix() {
        return graphMatrix;
    }

    public int getAttackLength() {
        return attackLength;
    }

    public boolean[] getIsTargetNode() {
        return isTargetNode;
    }
}
