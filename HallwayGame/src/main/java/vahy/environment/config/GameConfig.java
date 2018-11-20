package vahy.environment.config;

import vahy.environment.state.StateRepresentation;

public interface GameConfig {

    double getGoalReward();
    double getStepPenalty();
    double getTrapProbability();
    double getNoisyMoveProbability();
    StateRepresentation getStateRepresentation();
}
