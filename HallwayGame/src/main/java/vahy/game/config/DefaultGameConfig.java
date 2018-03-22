package vahy.game.config;

public class DefaultGameConfig implements IGameConfig{

    @Override
    public int getDefaultGoalReward() {
        return 1;
    }

    @Override
    public int getDefaultStepPenalty() {
        return -1;
    }

    @Override
    public double getDefaultTrapProbability() {
        return 0.1;
    }

    @Override
    public double getDefaultMissMoveProbability() {
        return 0.0;
    }
}
