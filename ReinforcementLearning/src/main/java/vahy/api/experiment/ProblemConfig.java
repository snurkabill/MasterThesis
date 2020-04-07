package vahy.api.experiment;

public abstract class ProblemConfig implements Config {

    private final int maximalStepCountBound;
    private final boolean isModelKnown;

    protected ProblemConfig(int maximalStepCountBound, boolean isModelKnown) {
        this.isModelKnown = isModelKnown;
        if(maximalStepCountBound == 0) {
            throw new IllegalArgumentException("MaximalStepCountBound must be positive. Actual value: [" + maximalStepCountBound + "]");
        }
        this.maximalStepCountBound = maximalStepCountBound;
    }

    public int getMaximalStepCountBound() {
        return maximalStepCountBound;
    }

    public boolean isModelKnown() {
        return isModelKnown;
    }

    @Override
    public String toString() {
        return "maximalStepCountBound," + maximalStepCountBound + System.lineSeparator() +
            "isModelKnown," + isModelKnown + System.lineSeparator();
    }

}
