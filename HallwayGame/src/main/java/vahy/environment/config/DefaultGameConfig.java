package vahy.environment.config;

public class DefaultGameConfig implements IGameConfig {

    private final double goalReward = 100;
    private final double stepPenalty = -1;
    private final double trapProbability = 0.1;
    private final double noisyMoveProbability = 0.1;

    @Override
    public double getDefaultGoalReward() {
        return goalReward;
    }

    @Override
    public double getDefaultStepPenalty() {
        return stepPenalty;
    }

    @Override
    public double getDefaultTrapProbability() {
        return trapProbability;
    }

    @Override
    public double getDefaultMissMoveProbability() {
        return noisyMoveProbability;
    }
}
