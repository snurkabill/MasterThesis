package vahy.environment.state;

import java.util.Random;

public class StaticStatePart {

    private final Random random;
    private final double[][] trapProbabilities;
    private final boolean[][] walls;
    private final double defaultStepPenalty;


    public StaticStatePart(Random random, double[][] trapProbabilities, boolean[][] walls, double defaultStepPenalty) {
        this.random = random;
        this.trapProbabilities = trapProbabilities;
        this.walls = walls;
        this.defaultStepPenalty = defaultStepPenalty;
    }

    public Random getRandom() {
        return random;
    }

    public double[][] getTrapProbabilities() {
        return trapProbabilities;
    }

    public boolean[][] getWalls() {
        return walls;
    }

    public double getDefaultStepPenalty() {
        return defaultStepPenalty;
    }
}
