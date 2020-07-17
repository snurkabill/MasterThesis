package vahy.examples.simplifiedHallway;

import vahy.api.episode.PolicyCategoryInfo;
import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.ProblemConfig;
import vahy.examples.simplifiedHallway.cell.Cell;
import vahy.impl.RoundBuilder;

import java.util.List;
import java.util.Set;
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
        super(maximalStepCountBound, isModelKnown, 1, 1, List.of(
            new PolicyCategoryInfo(false, RoundBuilder.ENVIRONMENT_CATEGORY_ID, 1),
            new PolicyCategoryInfo(false, RoundBuilder.ENVIRONMENT_CATEGORY_ID + 1, 1)), PolicyShuffleStrategy.NO_SHUFFLE);
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
