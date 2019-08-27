package vahy.environment;

public class RandomWalkSetup {

    private final int goalLevel;

    private final int upSafeShift;
    private final int downSafeShift;

    private final int upUnsafeShift;
    private final int downUnsafeShift;

    private final double upAfterSafeProbability;
    private final double upAfterUnsafeProbability;

    public RandomWalkSetup(int goalLevel,
                           int upSafeShift,
                           int downSafeShift,
                           int upUnsafeShift,
                           int downUnsafeShift,
                           double upAfterSafeProbability,
                           double upAfterUnsafeProbability) {
        this.goalLevel = goalLevel;
        this.upSafeShift = upSafeShift;
        this.downSafeShift = downSafeShift;
        this.upUnsafeShift = upUnsafeShift;
        this.downUnsafeShift = downUnsafeShift;
        this.upAfterSafeProbability = upAfterSafeProbability;
        this.upAfterUnsafeProbability = upAfterUnsafeProbability;
    }

    public int getGoalLevel() {
        return goalLevel;
    }

    public int getUpSafeShift() {
        return upSafeShift;
    }

    public int getDownSafeShift() {
        return downSafeShift;
    }

    public double getUpAfterSafeProbability() {
        return upAfterSafeProbability;
    }

    public double getUpAfterUnsafeProbability() {
        return upAfterUnsafeProbability;
    }

    public int getDownUnsafeShift() {
        return downUnsafeShift;
    }

    public int getUpUnsafeShift() {
        return upUnsafeShift;
    }
}
