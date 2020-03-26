package vahy.api.experiment;

public abstract class ProblemConfig implements Config {

    private final int maximalStepCountBound;

    protected ProblemConfig(int maximalStepCountBound) {
        if(maximalStepCountBound == 0) {
            throw new IllegalArgumentException("MaximalStepCountBound must be positive. Actual value: [" + maximalStepCountBound + "]");
        }
        this.maximalStepCountBound = maximalStepCountBound;
    }

    public int getMaximalStepCountBound() {
        return maximalStepCountBound;
    }

    @Override
    public String toString() {
        return "maximalStepCountBound," + maximalStepCountBound + System.lineSeparator();
    }

}
