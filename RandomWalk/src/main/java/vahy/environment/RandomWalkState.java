package vahy.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.StateRewardReturn;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperState;
import vahy.utils.ImmutableTuple;

import java.util.LinkedList;

public class RandomWalkState implements PaperState<RandomWalkAction, DoubleReward, DoubleVector, RandomWalkProbabilities, RandomWalkState> {

    private static final Logger logger = LoggerFactory.getLogger(RandomWalkState.class.getName());

    private final int level;
    private final int maximumSoFar;
    private final int stepCount;
    private final boolean isAgentTurn;
    private final RandomWalkAction previousAction;
    private final RandomWalkSetup randomWalkSetup;

    public RandomWalkState(RandomWalkSetup randomWalkSetup) {
        this(0, 0, 0, true, RandomWalkAction.DOWN, randomWalkSetup);
    }

    protected RandomWalkState(int level, int maximumSoFar, int stepCount, boolean isAgentTurn, RandomWalkAction previousAction, RandomWalkSetup randomWalkSetup) {
        this.level = level;
        this.maximumSoFar = maximumSoFar;
        this.stepCount = stepCount;
        this.isAgentTurn = isAgentTurn;
        this.previousAction = previousAction;
        this.randomWalkSetup = randomWalkSetup;
    }

    @Override
    public RandomWalkAction[] getAllPossibleActions() {
        if(isAgentTurn) {
            return RandomWalkAction.playerActions;
        } else {
            return RandomWalkAction.environmentActions;
        }
    }

    private int resolveNewLevel(RandomWalkAction appliedAction) {
        if(appliedAction == RandomWalkAction.UP) {
            return level + (previousAction == RandomWalkAction.SAFE ? randomWalkSetup.getUpSafeShift() : randomWalkSetup.getUpUnsafeShift());
        } else if(appliedAction == RandomWalkAction.DOWN) {
            return level - (previousAction == RandomWalkAction.SAFE ? randomWalkSetup.getDownSafeShift() : randomWalkSetup.getDownUnsafeShift());
        } else {
            return level;
        }
    }

    @Override
    public StateRewardReturn<RandomWalkAction, DoubleReward, DoubleVector, RandomWalkProbabilities, RandomWalkState> applyAction(RandomWalkAction actionType) {
        int newLevel = resolveNewLevel(actionType);
        int newMax = newLevel > maximumSoFar ? newLevel : maximumSoFar;
        int newStepCount = actionType.isPlayerAction() ? stepCount + 1 : stepCount;
        boolean isAgentTurnNext = !isAgentTurn;
        var nextState = new RandomWalkState(
            newLevel,
            newMax,
            newStepCount,
            isAgentTurnNext,
            actionType,
            randomWalkSetup);
//        DoubleReward reward = nextState.isFinalState() ? new DoubleReward((double) level) : new DoubleReward(0.0);
        DoubleReward reward = new DoubleReward((double) (newLevel - level));
        return new ImmutableStateRewardReturnTuple<>(nextState, reward);
    }

    @Override
    public RandomWalkState deepCopy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DoubleVector getPlayerObservation() {
        return new DoubleVector(new double[] {maximumSoFar - level});
    }

    @Override
    public RandomWalkProbabilities getOpponentObservation() {
        var possibleActions = new LinkedList<RandomWalkAction>();
        var actionProbabilities = new LinkedList<Double>();
        if(isAgentTurn) {
            return new RandomWalkProbabilities(new ImmutableTuple<>(possibleActions, actionProbabilities));
        } else {
            if(previousAction == RandomWalkAction.SAFE) {
                possibleActions.add(RandomWalkAction.UP);
                actionProbabilities.add(randomWalkSetup.getUpAfterSafeProbability());
                possibleActions.add(RandomWalkAction.DOWN);
                actionProbabilities.add(1.0 - randomWalkSetup.getUpAfterSafeProbability());
            } else {
                possibleActions.add(RandomWalkAction.UP);
                actionProbabilities.add(randomWalkSetup.getUpAfterUnsafeProbability());
                possibleActions.add(RandomWalkAction.DOWN);
                actionProbabilities.add(1.0 - randomWalkSetup.getUpAfterUnsafeProbability());
            }
        }
        return new RandomWalkProbabilities(new ImmutableTuple<>(possibleActions, actionProbabilities));
    }

    @Override
    public String readableStringRepresentation() {
        StringBuilder sb = new StringBuilder();
        return sb.append("Actual level: ").append(level).append(" Max so far: ").append(maximumSoFar).toString();
    }

    @Override
    public boolean isOpponentTurn() {
        return !isAgentTurn;
    }

    @Override
    public boolean isFinalState() {
        return isRiskHit() || stepCount >= randomWalkSetup.getStepBound();
    }

    @Override
    public boolean isRiskHit() {
        return maximumSoFar - level > randomWalkSetup.getLowerRiskBound();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RandomWalkState)) return false;

        RandomWalkState that = (RandomWalkState) o;

        if (level != that.level) return false;
        if (maximumSoFar != that.maximumSoFar) return false;
        if (stepCount != that.stepCount) return false;
        if (isAgentTurn != that.isAgentTurn) return false;
        return previousAction == that.previousAction;
    }

    @Override
    public int hashCode() {
        int result = level;
        result = 31 * result + maximumSoFar;
        result = 31 * result + stepCount;
        result = 31 * result + (isAgentTurn ? 1 : 0);
        result = 31 * result + previousAction.hashCode();
        return result;
    }
}
