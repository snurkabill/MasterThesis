package vahy.environment.config;

import vahy.api.experiment.ProblemConfig;
import vahy.environment.state.StateRepresentation;

public interface GameConfig extends ProblemConfig {

    double getGoalReward();
    double getStepPenalty();
    double getTrapProbability();
    double getNoisyMoveProbability();
    StateRepresentation getStateRepresentation();
}
