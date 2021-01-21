package vahy.ralph.policy.linearProgram;

public enum NoiseStrategy {

    NONE(0, 0),
    NOISY_03_04(Math.pow(10, -3), Math.pow(10, -4)),
    NOISY_05_06(Math.pow(10, -5), Math.pow(10, -6)),
    NOISY_10_11(Math.pow(10, -10), Math.pow(10, -11)),
    NOISY_14_15(Math.pow(10, -14), Math.pow(10, -15));


    private final double lowerBound;
    private final double upperBound;

    NoiseStrategy(double upperBound, double lowerBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }
}
