package vahy.environment;

public class RandomWalkSetup {

    private final int stepBound;
    private final int lowerRiskBound;
    private final int upSafeShift;
    private final int downSafeShift;

    private final int upUnsafeShift;
    private final int downUnsafeShift;

    private final double upAfterSafeProbability;
    private final double upAfterUnsafeProbability;

    public RandomWalkSetup(int stepBound,
                           int lowerRiskBound,
                           int upSafeShift,
                           int downSafeShift,
                           int upUnsafeShift,
                           int downUnsafeShift,
                           double upAfterSafeProbability,
                           double upAfterUnsafeProbability) {
        this.stepBound = stepBound;
        this.lowerRiskBound = lowerRiskBound;
        this.upSafeShift = upSafeShift;
        this.downSafeShift = downSafeShift;
        this.upUnsafeShift = upUnsafeShift;
        this.downUnsafeShift = downUnsafeShift;
        this.upAfterSafeProbability = upAfterSafeProbability;
        this.upAfterUnsafeProbability = upAfterUnsafeProbability;
    }

    public int getStepBound() {
        return stepBound;
    }

    public int getLowerRiskBound() {
        return lowerRiskBound;
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
