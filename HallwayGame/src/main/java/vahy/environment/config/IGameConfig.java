package vahy.environment.config;

public interface IGameConfig {

    double getDefaultGoalReward();
    double getDefaultStepPenalty();
    double getDefaultTrapProbability();
    double getDefaultNoisyMoveProbability();
}
