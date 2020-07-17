package vahy.api.experiment;

import vahy.api.episode.PolicyCategoryInfo;
import vahy.api.episode.PolicyShuffleStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ProblemConfig implements Config {

    private final int maximalStepCountBound;
    private final boolean isModelKnown;

    private final int environmentPolicyCount;
    private final int minimalPlayerEntitiesInGameCount;
    private final List<PolicyCategoryInfo> policyCategoryInfoList;
    private final PolicyShuffleStrategy policyShuffleStrategy;

    protected ProblemConfig(int maximalStepCountBound, boolean isModelKnown, int environmentPolicyCount, int minimalPlayerEntitiesInGameCount, List<PolicyCategoryInfo> policyCategoryInfoList, PolicyShuffleStrategy policyShuffleStrategy) {
        this.isModelKnown = isModelKnown;
        this.environmentPolicyCount = environmentPolicyCount;
        this.minimalPlayerEntitiesInGameCount = minimalPlayerEntitiesInGameCount;
        this.policyCategoryInfoList = policyCategoryInfoList;
        if(maximalStepCountBound == 0) {
            throw new IllegalArgumentException("MaximalStepCountBound must be positive. Actual value: [" + maximalStepCountBound + "]");
        }
        this.maximalStepCountBound = maximalStepCountBound;
        this.policyShuffleStrategy = policyShuffleStrategy;
        checkPolicyCategoryInfo(policyCategoryInfoList);
    }

    private void checkPolicyCategoryInfo(List<PolicyCategoryInfo> policyCategoryInfoList) {
        if(policyCategoryInfoList.isEmpty()) {
            throw new IllegalArgumentException("PlayerFreeSlots can't be empty");
        }

        var categoryIdListAsString = policyCategoryInfoList.stream().map(x -> String.valueOf(x.getCategoryId())).collect(Collectors.joining(", "));

        var map = new HashMap<Integer, PolicyCategoryInfo>(policyCategoryInfoList.size());
        for (PolicyCategoryInfo policyCategoryInfo : policyCategoryInfoList) {
            var categoryId = policyCategoryInfo.getCategoryId();
            if(categoryId < 0) {
                throw new IllegalStateException("Category ID can't be smaller than 0. Provided categoryIds: [" + categoryIdListAsString + "]");
            }
            if(map.containsKey(categoryId)) {
                throw new IllegalStateException("Given policy category set contains duplicates in id: [" + categoryId + "]");
            } else {
                map.put(categoryId, policyCategoryInfo);
            }
        }

        if(environmentPolicyCount > 0) {
            var minCategoryId = map.keySet().stream().min(Integer::compareTo).orElseThrow();
            if(minCategoryId != 0) {
                throw new IllegalArgumentException("Expected environment policy count. [" + environmentPolicyCount +
                    "] but provided no category with ID 0. Provided category Ids: [" + categoryIdListAsString + "]");
            }
            int environmentPoliciesCount = map.get(0).getPolicyInCategoryCount();
            if(environmentPoliciesCount != environmentPolicyCount) {
                throw new IllegalStateException("Different count of environment policies. Expected: [" + environmentPolicyCount + "] actual: [" + environmentPoliciesCount + "]");
            }
        }
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

    public List<PolicyCategoryInfo> getPolicyCategoryInfoList() {
        return policyCategoryInfoList;
    }

    public PolicyShuffleStrategy getPolicyShuffleStrategy() {
        return policyShuffleStrategy;
    }

    @Override
    public String toString() {
        return "maximalStepCountBound," + maximalStepCountBound + System.lineSeparator() +
            "isModelKnown," + isModelKnown + System.lineSeparator();
    }

}
