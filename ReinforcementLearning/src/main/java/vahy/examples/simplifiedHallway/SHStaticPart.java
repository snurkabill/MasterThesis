package vahy.examples.simplifiedHallway;

import java.util.Arrays;

public final class SHStaticPart {

    private final double[] noRewardGained;
    private final double[] environmentMovementReward;
    private final boolean[][] walls;
    private final double[][] trapProbabilities;
    private final int[][] rewardIds;

    private final double defaultStepPenalty;

    public SHStaticPart(boolean[][] walls, double[][] trapProbabilities, int[][] rewardIds,double defaultStepPenalty) {
        this.walls = walls;
        this.trapProbabilities = trapProbabilities;
        this.rewardIds = rewardIds;
        this.defaultStepPenalty = defaultStepPenalty;
        this.noRewardGained = new double[] {0.0, -defaultStepPenalty};
        this.environmentMovementReward = new double[] {0.0, 0.0};
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

    public double[] getNoRewardGained() {
        return noRewardGained;
    }

    public double[] getEnvironmentMovementReward() {
        return environmentMovementReward;
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
