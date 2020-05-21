package vahy.api.experiment;

import java.util.Set;

public abstract class ProblemConfig implements Config {

    private final int maximalStepCountBound;
    private final boolean isModelKnown;

    private final int environmentPolicyCount;
    private final int minimalPlayerEntitiesInGameCount;
    private final Set<Integer> freePlayerIdSlots;

    protected ProblemConfig(int maximalStepCountBound, boolean isModelKnown, int environmentPolicyCount, int minimalPlayerEntitiesInGameCount, Set<Integer> freePlayerIdSlots) {
        this.isModelKnown = isModelKnown;
        this.environmentPolicyCount = environmentPolicyCount;
        this.minimalPlayerEntitiesInGameCount = minimalPlayerEntitiesInGameCount;
        this.freePlayerIdSlots = freePlayerIdSlots;
        if(maximalStepCountBound == 0) {
            throw new IllegalArgumentException("MaximalStepCountBound must be positive. Actual value: [" + maximalStepCountBound + "]");
        }
        if(freePlayerIdSlots.isEmpty()) {
            throw new IllegalArgumentException("PlayerFreeSlots can't be empty");
        }
        if(freePlayerIdSlots.stream().min(Integer::compareTo).orElseThrow() != environmentPolicyCount) {
            throw new IllegalArgumentException("Different environmentPolicyCount: [" + environmentPolicyCount + "] with minimal id in freePlayerIdSlot");
        }
        this.maximalStepCountBound = maximalStepCountBound;
    }

    public int getMaximalStepCountBound() {
        return maximalStepCountBound;
    }

    public boolean isModelKnown() {
        return isModelKnown;
    }

    public int getMinimalPlayerEntitiesInGameCount() {
        return minimalPlayerEntitiesInGameCount;
    }

    public int getEnvironmentPolicyCount() {
        return environmentPolicyCount;
    }

    public Set<Integer> getFreePlayerIdSlots() {
        return freePlayerIdSlots;
    }

    @Override
    public String toString() {
        return "maximalStepCountBound," + maximalStepCountBound + System.lineSeparator() +
            "isModelKnown," + isModelKnown + System.lineSeparator();
    }

}
