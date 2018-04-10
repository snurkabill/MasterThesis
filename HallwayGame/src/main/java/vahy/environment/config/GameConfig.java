package vahy.environment.config;

public interface GameConfig {

    double getGoalReward();
    double getStepPenalty();
    double getTrapProbability();
    double getNoisyMoveProbability();
}
