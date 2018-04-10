package vahy.environment.config;

public class ConfigBuilder {

    private double goalReward;
    private double stepPenalty;
    private double trapProbability;
    private double noisyMoveProbability;

    public ConfigBuilder() {
        GameConfig defaultGameConfig = new DefaultGameConfig();
        goalReward = defaultGameConfig.getGoalReward();
        stepPenalty = defaultGameConfig.getStepPenalty();
        trapProbability = defaultGameConfig.getTrapProbability();
        noisyMoveProbability = defaultGameConfig.getNoisyMoveProbability();
    }

    public ConfigBuilder reward(double goalReward) {
        this.goalReward = goalReward;
        return this;
    }

    public ConfigBuilder stepPenalty(double stepPenalty) {
        this.stepPenalty = stepPenalty;
        return this;
    }

    public ConfigBuilder trapProbability(double trapProbability) {
        this.trapProbability = trapProbability;
        return this;
    }

    public ConfigBuilder noisyMoveProbability(double noisyMoveProbability) {
        this.noisyMoveProbability = noisyMoveProbability;
        return this;
    }

    public GameConfig buildConfig() {
        return new GameConfigImpl(goalReward, stepPenalty, trapProbability, noisyMoveProbability);
    }

}
