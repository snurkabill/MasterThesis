package vahy.environment.config;

import vahy.environment.state.StateRepresentation;

public class DefaultGameConfig implements GameConfig {

    private final double goalReward = 100;
    private final double stepPenalty = 1;
    private final double trapProbability = 0.0;
    private final double noisyMoveProbability = 0.1;
    private final StateRepresentation stateRepresentation = StateRepresentation.FULL;

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

    @Override
    public StateRepresentation getStateRepresentation() {
        return stateRepresentation;
    }
}
