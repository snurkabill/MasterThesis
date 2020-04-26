package vahy.paperGenerics.testDomain;

import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.testdomain.emptySpace.EmptySpaceAction;
import vahy.paperGenerics.PaperState;

import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;

public class EmptySpaceRiskState implements PaperState<EmptySpaceAction, DoubleVector, EmptySpaceRiskState, EmptySpaceRiskState>, Observation {

    private final SplittableRandom random;
    private final boolean isRiskHit;
    private final double riskProbability;
    private final boolean isPlayerTurn;

    public EmptySpaceRiskState(boolean isPlayerTurn, SplittableRandom random, boolean isRiskHit, double riskProbability) {
        this.isPlayerTurn = isPlayerTurn;
        this.riskProbability = riskProbability;
        this.random = random;
        this.isRiskHit = isRiskHit;
    }

    @Override
    public EmptySpaceAction[] getAllPossibleActions() {
        if(isPlayerTurn) {
            return EmptySpaceAction.playerActions;
        } else {
            return  EmptySpaceAction.opponentActions;
        }
    }

    @Override
    public EmptySpaceAction[] getPossiblePlayerActions() {
        return getAllPossibleActions();
    }

    @Override
    public EmptySpaceAction[] getPossibleOpponentActions() {
        return getAllPossibleActions();
    }

    @Override
    public StateRewardReturn<EmptySpaceAction, DoubleVector, EmptySpaceRiskState, EmptySpaceRiskState> applyAction(EmptySpaceAction actionType) {
        return new ImmutableStateRewardReturnTuple<>(new EmptySpaceRiskState(!isPlayerTurn, random, random.nextDouble() < riskProbability, riskProbability), random.nextDouble());
    }

    @Override
    public DoubleVector getPlayerObservation() {
        return new DoubleVector(new double[] {0.0});
    }

    @Override
    public EmptySpaceRiskState getOpponentObservation() {
        return this;
    }

    @Override
    public Predictor<EmptySpaceRiskState> getKnownModelWithPerfectObservationPredictor() {
        return new Predictor<>() {

            private double[] fixedPrediction = new double[] {1/3., 2/3.0};

            @Override
            public double[] apply(EmptySpaceRiskState observation) {
                return fixedPrediction;
            }

            @Override
            public double[][] apply(EmptySpaceRiskState[] observationArray) {
                var prediction = new double[observationArray.length][];
                Arrays.fill(prediction, fixedPrediction);
                return prediction;
            }
        };
    }

    @Override
    public String readableStringRepresentation() {
        return null;
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
    public boolean isOpponentTurn() {
        return !isPlayerTurn;
    }

    @Override
    public boolean isFinalState() {
        return isRiskHit;
    }

    @Override
    public boolean isRiskHit() {
        return isRiskHit;
    }
}
