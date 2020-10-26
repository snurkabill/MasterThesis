package vahy.examples.patrolling;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.predictor.Predictor;
import vahy.impl.model.ImmutableStateRewardReturn;
import vahy.impl.model.observation.DoubleVector;

import java.util.List;

public class PatrollingState implements State<PatrollingAction, DoubleVector, PatrollingState> {

    private static final int GUARD_ID = 0;
    private static final int ATTACKER_ID = 1;

    private static final double[] emptyReward = new double[] {0.0, 0.0};
    private static final double[] guardWinsReward = new double[] {1.0, -1.0};
    private static final double[] attackerWinsReward = new double[] {-1.0, 1.0};

    private final PatrollingStaticPart staticPart;

    private final boolean attackInProgress;
    private final int attackCountDown;
    private final int attackOnNodeId;

    private final boolean guardOnTurn;
    private final int guardOnNodeId;


    public PatrollingState(PatrollingStaticPart staticPart, int guardOnNodeId) {
        this(staticPart, guardOnNodeId, Integer.MAX_VALUE, false, Integer.MIN_VALUE, true);
    }

    private PatrollingState(PatrollingStaticPart staticPart, int guardOnNodeId, int attackCountDown, boolean attackInProgress, int attackOnNodeId, boolean guardOnTurn) {
        this.guardOnNodeId = guardOnNodeId;
        this.staticPart = staticPart;
        this.attackCountDown = attackCountDown;
        this.attackInProgress = attackInProgress;
        this.attackOnNodeId = attackOnNodeId;
        this.guardOnTurn = guardOnTurn;
    }

    @Override
    public PatrollingAction[] getAllPossibleActions(int inGameEntityId) {
        if(inGameEntityId == GUARD_ID) {
            if(guardOnTurn) {
                return staticPart.getPossibleMoveArray(guardOnNodeId);
            } else {
                return staticPart.getPossibleAttackArray();
            }
        } else {
            if(guardOnTurn) {
                return staticPart.getPossibleMoveArray(guardOnNodeId);
            } else {
                if(attackInProgress) {
                    return new PatrollingAction[] {PatrollingAction.WAIT};
                } else {
                    return staticPart.getPossibleAttackArray();
                }
            }
        }
    }

    @Override
    public int getTotalEntityCount() {
        return 2;
    }

    private double[] resolveReward(int attackCountDown, int attackOnNodeId, int guardOnNodeId) {
        if(attackCountDown == 0) {
            return attackerWinsReward;
        }
        if(attackOnNodeId == guardOnNodeId) {
            return guardWinsReward;
        }
        return emptyReward;
    }

    @Override
    public StateRewardReturn<PatrollingAction, DoubleVector, PatrollingState> applyAction(PatrollingAction actionType) {

        var newAttackCountDown = attackInProgress && guardOnTurn ? attackCountDown - 1 : attackCountDown;

        if(actionType == PatrollingAction.WAIT) {
            if(guardOnTurn) {
                throw new IllegalStateException("Guard can't play wait action");
            }
            return new ImmutableStateRewardReturn<>(
                new PatrollingState(staticPart, guardOnNodeId, newAttackCountDown, attackInProgress, attackOnNodeId, !guardOnTurn),
                resolveReward(newAttackCountDown, attackOnNodeId, guardOnNodeId),
                new PatrollingAction[] {PatrollingAction.SHADOW, actionType}
            );
        }
        if(actionType == PatrollingAction.SHADOW) {
            throw new IllegalStateException("This should never happen. We will see.");
        }

        if(guardOnTurn) {
            if(actionType.isAttackerTrueAction()) {
                throw new IllegalStateException("Discrepancy");
            }
            var moveToId = actionType.getLocalIndex();
            return new ImmutableStateRewardReturn<>(
                new PatrollingState(staticPart, moveToId, newAttackCountDown, attackInProgress, attackOnNodeId, !guardOnTurn),
                resolveReward(newAttackCountDown, attackOnNodeId, moveToId),
                new PatrollingAction[] {actionType, actionType}
            );
        } else {
            if(!actionType.isAttackerTrueAction()) {
                throw new IllegalStateException("Discrepancy");
            }

            var attackToId = actionType.getLocalIndex();
            return new ImmutableStateRewardReturn<>(
                new PatrollingState(staticPart, guardOnNodeId, staticPart.getAttackLength(), true, attackToId, !guardOnTurn),
                resolveReward(staticPart.getAttackLength(), attackToId, guardOnNodeId),
                new PatrollingAction[] {actionType, actionType}
            );
        }
    }

    @Override
    public DoubleVector getInGameEntityObservation(int inGameEntityId) {
        return getCommonObservation(inGameEntityId);
    }

    @Override
    public DoubleVector getCommonObservation(int inGameEntityId) {
        var arr = new double[staticPart.getNodeCount()];
        arr[guardOnNodeId] = 1;
        return new DoubleVector(arr);
    }

    @Override
    public Predictor<PatrollingState> getKnownModelWithPerfectObservationPredictor() {
        throw new UnsupportedOperationException("PatrollingGame does not have fixed model");
    }

    @Override
    public String readableStringRepresentation() {
        return "Not Implemented now";
    }

    @Override
    public List<String> getCsvHeader() {
        return null;
    }

    @Override
    public List<String> getCsvRecord() {
        return null;
    }

    @Override
    public int getInGameEntityIdOnTurn() {
        return guardOnTurn ? GUARD_ID : ATTACKER_ID;
    }

    @Override
    public boolean isEnvironmentEntityOnTurn() {
        return false;
    }

    @Override
    public boolean isInGame(int inGameEntityId) {
        return true;
    }

    @Override
    public boolean isFinalState() {
        return (attackCountDown == 0) || (attackInProgress && attackOnNodeId == guardOnNodeId);
    }
}
