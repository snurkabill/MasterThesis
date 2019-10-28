package vahy.environment.config;

import vahy.environment.state.StateRepresentation;
import vahy.game.HallwayInstance;

public interface GameConfig {

    double getGoalReward();
    double getStepPenalty();
    double getTrapProbability();
    double getNoisyMoveProbability();
    StateRepresentation getStateRepresentation();
    HallwayInstance getHallwayInstance();
}
