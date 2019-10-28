package vahy.environment.config;

import vahy.environment.state.StateRepresentation;
import vahy.game.HallwayInstance;

public class GameConfigImpl implements GameConfig {

    private final double goalReward;
    private final double stepPenalty;
    private final double trapProbability;
    private final double noisyMoveProbability;
    private final StateRepresentation stateRepresentation;
    private final HallwayInstance hallwayInstance;

    public GameConfigImpl(double goalReward, double stepPenalty, double trapProbability, double noisyMoveProbability, StateRepresentation stateRepresentation, HallwayInstance hallwayInstance) {
        this.goalReward = goalReward;
        this.stepPenalty = stepPenalty;
        this.trapProbability = trapProbability;
        this.noisyMoveProbability = noisyMoveProbability;
        this.stateRepresentation = stateRepresentation;
        this.hallwayInstance = hallwayInstance;
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
    public HallwayInstance getHallwayInstance() {
        return hallwayInstance;
    }

    @Override
    public String toString() {
        return "goalReward," + goalReward + System.lineSeparator() +
            "stepPenalty," + stepPenalty + System.lineSeparator() +
            "trapProbability," + trapProbability + System.lineSeparator() +
            "noisyMoveProbability," + noisyMoveProbability + System.lineSeparator() +
            "stateRepresentation," + stateRepresentation + System.lineSeparator() +
            "hallwayInstance," + hallwayInstance.name() + System.lineSeparator() +
            "hallwayInstancePath," + hallwayInstance.getPath() + System.lineSeparator();
    }
}
