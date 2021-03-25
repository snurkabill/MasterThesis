package vahy.examples.conquering;

import vahy.api.episode.PolicyCategoryInfo;
import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.ProblemConfig;
import vahy.impl.RoundBuilder;

import java.util.List;

public class ConqueringConfig extends ProblemConfig {

    private final int winReward;
    private final int stepPenalty;
    private final int playerCount;
    private final int hallwayLength;
    private final double killProbability;

    public ConqueringConfig(int maximalStepCountBound, PolicyShuffleStrategy policyShuffleStrategy, int winReward, int stepPenalty, int playerCount, int hallwayLength, double killProbability) {
        super(maximalStepCountBound, true, 1, 2,
            List.of(
                new PolicyCategoryInfo(false, RoundBuilder.ENVIRONMENT_CATEGORY_ID, 1),
                new PolicyCategoryInfo(true, RoundBuilder.ENVIRONMENT_CATEGORY_ID + 1, playerCount)),
            policyShuffleStrategy);
        this.winReward = winReward;
        this.stepPenalty = stepPenalty;
        this.playerCount = playerCount;
        this.hallwayLength = hallwayLength;
        this.killProbability = killProbability;
    }

    public int getWinReward() {
        return winReward;
    }

    public int getStepPenalty() {
        return stepPenalty;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public int getHallwayLength() {
        return hallwayLength;
    }

    public double getKillProbability() {
        return killProbability;
    }

    @Override
    public String toLog() {
        return null;
    }

    @Override
    public String toFile() {
        return null;
    }
}
