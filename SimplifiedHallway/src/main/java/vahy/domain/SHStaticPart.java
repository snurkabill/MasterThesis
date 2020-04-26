package vahy.domain;

import java.util.Arrays;

public class SHStaticPart {

    private final boolean[][] walls;
    private final double[][] trapProbabilities;
    private final int[][] rewardIds;

    private final double defaultStepPenalty;

    public SHStaticPart(boolean[][] walls, double[][] trapProbabilities, int[][] rewardIds,double defaultStepPenalty) {
        this.walls = walls;
        this.trapProbabilities = trapProbabilities;
        this.rewardIds = rewardIds;
        this.defaultStepPenalty = defaultStepPenalty;
    }

    public boolean[][] getWalls() {
        return walls;
    }

    public double[][] getTrapProbabilities() {
        return trapProbabilities;
    }

    public double getDefaultStepPenalty() {
        return defaultStepPenalty;
    }

    public int[][] getRewardIds() {
        return rewardIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SHStaticPart that = (SHStaticPart) o;

        if (Double.compare(that.defaultStepPenalty, defaultStepPenalty) != 0) return false;
        if (!Arrays.deepEquals(walls, that.walls)) return false;
        if (!Arrays.deepEquals(trapProbabilities, that.trapProbabilities)) return false;
        return Arrays.deepEquals(rewardIds, that.rewardIds);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = Arrays.deepHashCode(walls);
        result = 31 * result + Arrays.deepHashCode(trapProbabilities);
        result = 31 * result + Arrays.deepHashCode(rewardIds);
        temp = Double.doubleToLongBits(defaultStepPenalty);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
