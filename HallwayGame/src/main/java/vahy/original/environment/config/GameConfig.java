package vahy.original.environment.config;

import vahy.api.experiment.ProblemConfig;
import vahy.original.environment.state.StateRepresentation;
import vahy.original.game.cell.Cell;

import java.util.List;

public class GameConfig implements ProblemConfig {

    private final double goalReward;
    private final double stepPenalty;
    private final double trapProbability;
    private final double noisyMoveProbability;
    private final StateRepresentation stateRepresentation;
    private final String gameStringRepresentation;
    private final List<List<Cell>> gameMatrix;

    public GameConfig(double goalReward, double stepPenalty, double trapProbability, double noisyMoveProbability, StateRepresentation stateRepresentation, String gameStringRepresentation, List<List<Cell>> gameMatrix) {
        this.goalReward = goalReward;
        this.stepPenalty = stepPenalty;
        this.trapProbability = trapProbability;
        this.noisyMoveProbability = noisyMoveProbability;
        this.stateRepresentation = stateRepresentation;
        this.gameStringRepresentation = gameStringRepresentation;
        this.gameMatrix = gameMatrix;
    }

    public double getGoalReward() {
        return goalReward;
    }

    public double getStepPenalty() {
        return stepPenalty;
    }

    public double getTrapProbability() {
        return trapProbability;
    }

    public double getNoisyMoveProbability() {
        return noisyMoveProbability;
    }

    public String getGameStringRepresentation() {
        return gameStringRepresentation;
    }

    public StateRepresentation getStateRepresentation() {
        return stateRepresentation;
    }

    public List<List<Cell>> getGameMatrix() {
        return gameMatrix;
    }

    @Override
    public String toString() {
        return "goalReward," + goalReward + System.lineSeparator() +
            "stepPenalty," + stepPenalty + System.lineSeparator() +
            "trapProbability," + trapProbability + System.lineSeparator() +
            "noisyMoveProbability," + noisyMoveProbability + System.lineSeparator() +
            "stateRepresentation," + stateRepresentation + System.lineSeparator();
    }

    @Override
    public String toLog() {
        return toString();
    }

    @Override
    public String toFile() {
        return toString();
    }
}
