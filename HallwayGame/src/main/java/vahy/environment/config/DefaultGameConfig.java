package vahy.environment.config;

public class DefaultGameConfig implements GameConfig {

    private final double goalReward = 100;
    private final double stepPenalty = 1;
    private final double trapProbability = 0.0;
    private final double noisyMoveProbability = 0.1;

    @Override
    public double getGoalReward() {
        return goalReward;
    }

    @Override
    public double getStepPenalty() {
        return stepPenalty;
    }

    @Override
    public double getTrapProbability() {
        return trapProbability;
    }

    @Override
    public double getNoisyMoveProbability() {
        return noisyMoveProbability;
    }
}
