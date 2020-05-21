package vahy.examples.simplifiedHallway;

import vahy.api.experiment.ProblemConfig;
import vahy.examples.simplifiedHallway.cell.Cell;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SHConfig extends ProblemConfig {

    private final double goalReward;
    private final double stepPenalty;
    private final double trapProbability;
    private final String gameStringRepresentation;
    private final List<List<Cell>> gameMatrix;

    public SHConfig(int maximalStepCountBound,
                      boolean isModelKnown,
                      double goalReward,
                      double stepPenalty,
                      double trapProbability,
                      String gameStringRepresentation,
                      List<List<Cell>> gameMatrix) {
        super(maximalStepCountBound, isModelKnown, 1, 1, Stream.of(1).collect(Collectors.toSet()));
        this.goalReward = goalReward;
        this.stepPenalty = stepPenalty;
        this.trapProbability = trapProbability;
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

    public String getGameStringRepresentation() {
        return gameStringRepresentation;
    }

    public List<List<Cell>> getGameMatrix() {
        return gameMatrix;
    }

    @Override
    public String toString() {
        return super.toString() +
            "goalReward," + goalReward + System.lineSeparator() +
            "stepPenalty," + stepPenalty + System.lineSeparator() +
            "trapProbability," + trapProbability + System.lineSeparator();
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
