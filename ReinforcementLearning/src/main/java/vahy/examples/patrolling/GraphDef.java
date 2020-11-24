package vahy.examples.patrolling;

import java.util.Map;
import java.util.Set;

public class GraphDef {

    private final boolean[][] connectionMatrix;
    private final int[][] moveCostMatrix;

    private final Set<Integer> isTargetSet;
    private final Map<Integer, Integer> attackLengthMap;
    private final Map<Integer, Integer> attackCostMap;

    public GraphDef(boolean[][] connectionMatrix, int[][] moveCostMatrix, Set<Integer> isTargetSet, Map<Integer, Integer> attackLengthMap, Map<Integer, Integer> attackCostMap) {
        this.connectionMatrix = connectionMatrix;
        this.moveCostMatrix = moveCostMatrix;
        this.isTargetSet = isTargetSet;
        this.attackLengthMap = attackLengthMap;
        this.attackCostMap = attackCostMap;
    }

    public boolean[][] getConnectionMatrix() {
        return connectionMatrix;
    }

    public int[][] getMoveCostMatrix() {
        return moveCostMatrix;
    }

    public boolean isTargetSet(int nodeId) {
        return isTargetSet.contains(nodeId);
    }

    public int getAttackLength(int nodeId) {
        return attackLengthMap.get(nodeId);
    }

    public int getAttackCost(int nodeId) {
        return attackCostMap.get(nodeId);
    }
}
