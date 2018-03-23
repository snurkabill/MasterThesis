package vahy.environment.state;

import java.util.Random;

public class StaticGamePart {

    private final Random random;
    private final double[][] trapProbabilities;
    private final boolean[][] walls;
    private final double defaultStepPenalty;
    private final double noisyMoveProbability;


    public StaticGamePart(Random random, double[][] trapProbabilities, boolean[][] walls, double defaultStepPenalty, double noisyMoveProbability) {
        this.random = random;
        this.trapProbabilities = trapProbabilities;
        this.walls = walls;
        this.defaultStepPenalty = defaultStepPenalty;
        this.noisyMoveProbability = noisyMoveProbability;
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

    public double getNoisyMoveProbability() {
        return noisyMoveProbability;
    }
}
