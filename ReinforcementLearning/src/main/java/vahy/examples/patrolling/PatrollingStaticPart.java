package vahy.examples.patrolling;

import java.util.ArrayList;
import java.util.Arrays;

public class PatrollingStaticPart {

    private final boolean[][] graphRepresentation;
    private final int attackLength;

    private final PatrollingAction[] possibleAttackArray;
    private final PatrollingAction[] possibleMoveArray;

    private final PatrollingAction[][] possibleMoves;

    public PatrollingStaticPart(boolean[][] graphRepresentation, int attackLength) {
        this.graphRepresentation = graphRepresentation;
        this.attackLength = attackLength;
        this.possibleAttackArray = Arrays.stream(PatrollingAction.values()).filter(x -> (x.getLocalIndex() < graphRepresentation.length && x.isAttackerTrueAction()) || x.equals(PatrollingAction.WAIT)).toArray(PatrollingAction[]::new);
        this.possibleMoveArray = Arrays.stream(PatrollingAction.values()).filter(x -> x.getLocalIndex() < graphRepresentation.length  && !x.isAttackerTrueAction() && !x.equals(PatrollingAction.SHADOW)).toArray(PatrollingAction[]::new);
        this.possibleMoves = new PatrollingAction[graphRepresentation.length][];

        for (int i = 0; i < graphRepresentation.length; i++) {
            getPossibleMoveArray(i);
        }

    }

    public boolean[][] getGraphRepresentation() {
        return graphRepresentation;
    }

    public int getAttackLength() {
        return attackLength;
    }

    public int getNodeCount() {
        return graphRepresentation.length;
    }

    public PatrollingAction[] getPossibleAttackArray() {
        return possibleAttackArray;
    }

    public PatrollingAction[] getPossibleMoveArray(int guardOnNodeId) {
        var arr = possibleMoves[guardOnNodeId];
        if(arr == null) {
            var list = new ArrayList<PatrollingAction>(graphRepresentation.length);
            for (PatrollingAction patrollingAction : possibleMoveArray) {
                if(patrollingAction.getLocalIndex() >= graphRepresentation.length) {
                    break;
                } else {
                    if(graphRepresentation[guardOnNodeId][patrollingAction.getLocalIndex()]) {
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
