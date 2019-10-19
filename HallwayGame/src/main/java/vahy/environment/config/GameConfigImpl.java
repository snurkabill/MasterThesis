package vahy.environment.config;

import vahy.environment.state.StateRepresentation;

public class GameConfigImpl implements GameConfig {

    private final double goalReward;
    private final double stepPenalty;
    private final double trapProbability;
    private final double noisyMoveProbability;
    private final StateRepresentation stateRepresentation;

    public GameConfigImpl(double goalReward, double stepPenalty, double trapProbability, double noisyMoveProbability, StateRepresentation stateRepresentation) {
        this.goalReward = goalReward;
        this.stepPenalty = stepPenalty;
        this.trapProbability = trapProbability;
        this.noisyMoveProbability = noisyMoveProbability;
        this.stateRepresentation = stateRepresentation;
    }

    public double getGoalReward() {
        return goalReward;
    }

    public double getStepPenalty() {
        return stepPenalty;
    }

    public double getTrapProbability() {
        return trapProbability;
    }

    public double getNoisyMoveProbability() {
        return noisyMoveProbability;
    }

    @Override
    public StateRepresentation getStateRepresentation() {
        return stateRepresentation;
    }

    @Override
    public String toString() {
        return "goalReward," + goalReward + System.lineSeparator() +
            "stepPenalty," + stepPenalty + System.lineSeparator() +
            "trapProbability," + trapProbability + System.lineSeparator() +
            "noisyMoveProbability," + noisyMoveProbability + System.lineSeparator() +
            "stateRepresentation," + stateRepresentation + System.lineSeparator();
    }
}
