package vahy.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;

import java.util.Collections;
import java.util.List;

public class RandomWalkState implements PaperState<RandomWalkAction, DoubleVector, RandomWalkState, RandomWalkState>, Observation {

    private static final Logger logger = LoggerFactory.getLogger(RandomWalkState.class.getName());

    private final int level;
    private final boolean isAgentTurn;
    private final RandomWalkAction previousAction;
    private final RandomWalkSetup randomWalkSetup;

    public RandomWalkState(RandomWalkSetup randomWalkSetup) {
        this(randomWalkSetup.getStartLevel(), true, RandomWalkAction.DOWN, randomWalkSetup);
    }

    protected RandomWalkState(int level, boolean isAgentTurn, RandomWalkAction previousAction, RandomWalkSetup randomWalkSetup) {
        this.level = level;
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

    @Override
    public RandomWalkAction[] getPossiblePlayerActions() {
        if(isAgentTurn) {
            return RandomWalkAction.playerActions;
        } else {
            return new RandomWalkAction[0];
        }
    }

    @Override
    public RandomWalkAction[] getPossibleOpponentActions() {
        if(!isAgentTurn) {
            return RandomWalkAction.environmentActions;
        } else {
            return new RandomWalkAction[0];
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
    public StateRewardReturn<RandomWalkAction, DoubleVector, RandomWalkState, RandomWalkState> applyAction(RandomWalkAction actionType) {
        int newLevel = resolveNewLevel(actionType);
        boolean isAgentTurnNext = !isAgentTurn;
        var nextState = new RandomWalkState(
            newLevel,
            isAgentTurnNext,
            actionType,
            randomWalkSetup);
        var reward = actionType.isPlayerAction() ? 0.0 : ((newLevel - level) - randomWalkSetup.getStepPenalty());
        return new ImmutableStateRewardReturnTuple<>(nextState, reward);
    }

    @Override
    public DoubleVector getPlayerObservation() {
        return new DoubleVector(new double[] {level, previousAction.getGlobalIndex()});
    }

    @Override
    public RandomWalkState getOpponentObservation() {
        return this;
    }

    @Override
    public Predictor<RandomWalkState> getKnownModelWithPerfectObservationPredictor() {
        return new Predictor<>() {

            private final double[] safeProbabilities = new double[] {randomWalkSetup.getUpAfterSafeProbability(), 1.0 - randomWalkSetup.getUpAfterSafeProbability()};
            private final double[] unsafeProbabilities = new double[] {randomWalkSetup.getUpAfterUnsafeProbability(), 1.0 - randomWalkSetup.getUpAfterUnsafeProbability()};

            @Override
            public double[] apply(RandomWalkState observation) {
                if(observation.previousAction == RandomWalkAction.SAFE) {
                    return safeProbabilities;
                } else if(observation.previousAction == RandomWalkAction.UNSAFE) {
                    return unsafeProbabilities;
                } else {
                    throw new IllegalStateException("Not recognized previous action [" + observation.previousAction + "]. There must be player action");
                }
            }

            @Override
            public double[][] apply(RandomWalkState[] observationArray) {
                var prediction = new double[observationArray.length][];
                for (int i = 0; i < prediction.length; i++) {
                    prediction[i] = apply(observationArray[i]);
                }
                return prediction;
            }
        };

    }


    @Override
    public String readableStringRepresentation() {
        StringBuilder sb = new StringBuilder();
        return sb.append("Actual level: ").append(level).toString();
    }

    @Override
    public List<String> getCsvHeader() {
        return Collections.singletonList("Level");
    }

    @Override
    public List<String> getCsvRecord() {
        return Collections.singletonList(String.valueOf(level));
    }

    @Override
    public boolean isOpponentTurn() {
        return !isAgentTurn;
    }

    @Override
    public boolean isFinalState() {
        return isRiskHit() || level >= randomWalkSetup.getGoalLevel();
    }

    @Override
    public boolean isRiskHit() {
        return level < 0.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RandomWalkState)) return false;

        RandomWalkState that = (RandomWalkState) o;

        if (level != that.level) return false;
        if (isAgentTurn != that.isAgentTurn) return false;
        return previousAction == that.previousAction;
    }

    @Override
    public int hashCode() {
        int result = level;
        result = 31 * result + (isAgentTurn ? 1 : 0);
        result = 31 * result + previousAction.hashCode();
        return result;
    }
}
