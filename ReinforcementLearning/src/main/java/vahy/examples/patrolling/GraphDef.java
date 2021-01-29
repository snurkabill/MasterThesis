package vahy.examples.patrolling;

import java.util.Map;
import java.util.Set;

public class GraphDef {

    private final boolean[][] connectionMatrix;
    private final double[][] moveCostMatrix;

    private final Set<Integer> isTargetSet;
    private final Map<Integer, Double> attackLengthMap;
    private final Map<Integer, Double> attackCostMap;

    public GraphDef(boolean[][] connectionMatrix, double[][] moveCostMatrix, Set<Integer> isTargetSet, Map<Integer, Double> attackLengthMap, Map<Integer, Double> attackCostMap) {
        this.connectionMatrix = connectionMatrix;
        this.moveCostMatrix = moveCostMatrix;
        this.isTargetSet = isTargetSet;
        this.attackLengthMap = attackLengthMap;
        this.attackCostMap = attackCostMap;
    }

    public int nodeCount() {
        return connectionMatrix.length;
    }

    public boolean[][] getConnectionMatrix() {
        return connectionMatrix;
    }

    public double[][] getMoveCostMatrix() {
        return moveCostMatrix;
    }

    public boolean isTargetSet(int nodeId) {
        return isTargetSet.contains(nodeId);
    }

    public double getAttackLength(int nodeId) {
        return attackLengthMap.get(nodeId);
    }

    public double getAttackCost(int nodeId) {
        return attackCostMap.get(nodeId);
    }
}
