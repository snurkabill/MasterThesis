package vahy.examples.patrolling;

import java.util.ArrayList;
import java.util.Arrays;

public class PatrollingStaticPart {

    private final GraphDef graphRepresentation;

    private final PatrollingAction[] possibleAttackArray;
    private final PatrollingAction[] possibleMoveArray;

    private final PatrollingAction[][] possibleMoves;

    public PatrollingStaticPart(GraphDef graphRepresentation) {
        this.graphRepresentation = graphRepresentation;
        this.possibleAttackArray = Arrays.stream(PatrollingAction.values())
            .filter(x -> (graphRepresentation.isTargetSet(x.getLocalIndex()) && x.isAttackerTrueAction()) || x.equals(PatrollingAction.WAIT))
            .toArray(PatrollingAction[]::new);
        this.possibleMoveArray = Arrays.stream(PatrollingAction.values())
            .filter(x -> x.getLocalIndex() < graphRepresentation.getConnectionMatrix().length  && !x.isAttackerTrueAction() && !x.equals(PatrollingAction.SHADOW))
            .toArray(PatrollingAction[]::new);
        this.possibleMoves = new PatrollingAction[graphRepresentation.getConnectionMatrix().length][];

        for (int i = 0; i < graphRepresentation.getConnectionMatrix().length; i++) {
            getPossibleMoveArray(i);
        }

    }

    public GraphDef getGraphRepresentation() {
        return graphRepresentation;
    }

    public boolean[][] getConnectionMatrix() {
        return graphRepresentation.getConnectionMatrix();
    }

    public double getMoveTimeCost(int fromNodeId, int toNodeId) {
        return graphRepresentation.getMoveCostMatrix()[fromNodeId][toNodeId];
   }

    public double getAttackLength(int nodeId) {
        return graphRepresentation.getAttackLength(nodeId);
    }

    public int getNodeCount() {
        return graphRepresentation.getConnectionMatrix().length;
    }

    public PatrollingAction[] getPossibleAttackArray() {
        return possibleAttackArray;
    }

    public PatrollingAction[] getPossibleMoveArray(int guardOnNodeId) {
        var arr = possibleMoves[guardOnNodeId];
        if(arr == null) {
            var list = new ArrayList<PatrollingAction>(graphRepresentation.getConnectionMatrix().length);
            for (PatrollingAction patrollingAction : possibleMoveArray) {
                if(patrollingAction.getLocalIndex() >= graphRepresentation.getConnectionMatrix().length) {
                    break;
                } else {
                    if(graphRepresentation.getConnectionMatrix()[guardOnNodeId][patrollingAction.getLocalIndex()]) {
                        list.add(patrollingAction);
                    }
                }
            }
            if(list.isEmpty()) {
                throw new IllegalStateException("there must be at least one node where to go. Discrepancy");
            }
            possibleMoves[guardOnNodeId] = list.toArray(PatrollingAction[]::new);
            return possibleMoves[guardOnNodeId];
        } else {
            return arr;
        }
    }
}
