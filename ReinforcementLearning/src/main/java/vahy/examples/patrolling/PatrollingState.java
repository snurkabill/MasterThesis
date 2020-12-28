package vahy.examples.patrolling;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.predictor.PerfectStatePredictor;
import vahy.impl.model.ImmutableStateRewardReturn;
import vahy.impl.model.observation.DoubleVector;

import java.util.List;

public class PatrollingState implements State<PatrollingAction, DoubleVector, PatrollingState> {

    public static final int DEFENDER_ID = 0;
    public static final int ATTACKER_ID = 1;

    private static final double[] emptyReward = new double[] {0.0, 0.0};
//    private static final double[] defenderWinsReward = new double[] {1.0, 0.0};
//    private static final double[] attackerWinsReward = new double[] {0.0, 1.0};

    private final PatrollingStaticPart staticPart;

    private final boolean attackInProgress;
    private final double attackCountDown;
    private final int attackOnNodeId;

    private final boolean defenderOnTurn;
    private final int defenderOnNodeId;


    public PatrollingState(PatrollingStaticPart staticPart, int defenderOnNodeId) {
        this(staticPart, defenderOnNodeId, Double.MAX_VALUE, false, Integer.MIN_VALUE, true);
    }

    private PatrollingState(PatrollingStaticPart staticPart, int defenderOnNodeId, double attackCountDown, boolean attackInProgress, int attackOnNodeId, boolean defenderOnTurn) {
        this.defenderOnNodeId = defenderOnNodeId;
        this.staticPart = staticPart;
        this.attackCountDown = attackCountDown;
        this.attackInProgress = attackInProgress;
        this.attackOnNodeId = attackOnNodeId;
        this.defenderOnTurn = defenderOnTurn;
    }

    @Override
    public PatrollingAction[] getAllPossibleActions(int inGameEntityId) {
        if(inGameEntityId == DEFENDER_ID) {
            if(defenderOnTurn) {
                return staticPart.getPossibleMoveArray(defenderOnNodeId);
            } else {
                return staticPart.getPossibleAttackArray();
            }
        } else {
            if(defenderOnTurn) {
                return staticPart.getPossibleMoveArray(defenderOnNodeId);
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

    private double[] resolveReward(double attackCountDown, int attackOnNodeId, int defenderOnNodeId) {
        if(attackCountDown <= 0.0 && !defenderOnTurn) {
//            var cost = this.staticPart.getGraphRepresentation().getAttackCost(attackOnNodeId);
            return new double[] {0, 1};
        }
        if(attackOnNodeId == defenderOnNodeId && !defenderOnTurn) {
            return new double[] {1, 0};
        }
        return emptyReward;
    }

    @Override
    public StateRewardReturn<PatrollingAction, DoubleVector, PatrollingState> applyAction(PatrollingAction actionType) {


        if(actionType == PatrollingAction.WAIT) {
            if(defenderOnTurn) {
                throw new IllegalStateException("defender can't play wait action");
            }
            var newAttackCountDown = attackCountDown;
            return new ImmutableStateRewardReturn<>(
                new PatrollingState(staticPart, defenderOnNodeId, newAttackCountDown, attackInProgress, attackOnNodeId, !defenderOnTurn),
                resolveReward(newAttackCountDown, attackOnNodeId, defenderOnNodeId),
                new PatrollingAction[] {PatrollingAction.SHADOW, actionType}
            );
        }
        if(actionType == PatrollingAction.SHADOW) {
            throw new IllegalStateException("This should never happen. We will see.");
        }

        if(defenderOnTurn) {
            if(actionType.isAttackerTrueAction()) {
                throw new IllegalStateException("Discrepancy");
            }
            var moveToId = actionType.getLocalIndex();
            var moveTimeCost = staticPart.getMoveTimeCost(defenderOnNodeId, moveToId);
            var newAttackCountDown = attackInProgress && defenderOnTurn ? attackCountDown - moveTimeCost : attackCountDown;
            return new ImmutableStateRewardReturn<>(
                new PatrollingState(staticPart, moveToId, newAttackCountDown, attackInProgress, attackOnNodeId, !defenderOnTurn),
                resolveReward(newAttackCountDown, attackOnNodeId, moveToId),
                new PatrollingAction[] {actionType, actionType}
            );
        } else {
            if(!actionType.isAttackerTrueAction()) {
                throw new IllegalStateException("Discrepancy");
            }
            var attackToId = actionType.getLocalIndex();
            var attackLength = staticPart.getAttackLength(attackToId);
            return new ImmutableStateRewardReturn<>(
                new PatrollingState(staticPart, defenderOnNodeId, attackLength, true, attackToId, !defenderOnTurn),
                resolveReward(attackLength, attackToId, defenderOnNodeId),
                new PatrollingAction[] {actionType, actionType}
            );
        }
    }

    @Override
    public DoubleVector getInGameEntityObservation(int inGameEntityId) {
        if(inGameEntityId == DEFENDER_ID) {
            return getCommonObservation(inGameEntityId);
        } else {
            var arr = new double[staticPart.getNodeCount() * 2 + 2];
            arr[defenderOnNodeId] = 1;
            arr[staticPart.getNodeCount()] = defenderOnTurn ? 1.0 : 0.0;
            if(attackInProgress) {
                arr[staticPart.getNodeCount() + attackOnNodeId + 1] = 1;
                arr[arr.length - 1] = 1;
            } else {
                arr[arr.length - 1] = -1;
            }
            return new DoubleVector(arr);
        }
    }

    @Override
    public DoubleVector getCommonObservation(int inGameEntityId) {
//        var arr = new double[staticPart.getNodeCount() + 1];
//        arr[defenderOnNodeId] = 1;
//        arr[arr.length - 1] = defenderOnTurn ? 1.0 : 0.0;
//        return new DoubleVector(arr);

        var arr = new double[2];
        arr[0] = defenderOnNodeId;
        arr[1] = defenderOnTurn ? 1.0 : 0.0;
        return new DoubleVector(arr);
    }

    @Override
    public PerfectStatePredictor<PatrollingAction, DoubleVector, PatrollingState> getKnownModelWithPerfectObservationPredictor() {
        throw new UnsupportedOperationException("PatrollingGame does not have fixed model");
    }

    @Override
    public String readableStringRepresentation() {
        return "defender on Id: [" + defenderOnNodeId + "], Attack in progress: [" + attackInProgress + "] on Id: [" + attackOnNodeId + "] remaining attack countdown: [" + attackCountDown + "]";
    }

    @Override
    public String toString() {
        return readableStringRepresentation();
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
        return defenderOnTurn ? DEFENDER_ID : ATTACKER_ID;
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
        return (attackCountDown <= 0 && defenderOnTurn) || (attackInProgress && attackOnNodeId == defenderOnNodeId && defenderOnTurn);
    }

    public int getDefenderOnId() {
        return defenderOnNodeId;
    }

    public int getAttackOnId() {
        return attackOnNodeId;
    }

    public double getAttackCountdown() {
        return attackCountDown;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof PatrollingState)) return false;

        PatrollingState that = (PatrollingState) o;

        if (attackInProgress != that.attackInProgress) return false;
        if (Double.compare(that.attackCountDown, attackCountDown) != 0) return false;
        if (attackOnNodeId != that.attackOnNodeId) return false;
        if (defenderOnTurn != that.defenderOnTurn) return false;
        return defenderOnNodeId == that.defenderOnNodeId;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (attackInProgress ? 1 : 0);
        temp = Double.doubleToLongBits(attackCountDown);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + attackOnNodeId;
        result = 31 * result + (defenderOnTurn ? 1 : 0);
        result = 31 * result + defenderOnNodeId;
        return result;
    }
}
