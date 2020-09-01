package vahy.examples.patrolling;

import vahy.api.episode.PolicyCategoryInfo;
import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.ProblemConfig;

import java.util.List;

public class PatrollingConfig extends ProblemConfig {

    private final boolean[][] graphMatrix;
    private final int attackLength;
    private final boolean[] isTargetNode;

    protected PatrollingConfig(int maximalStepCountBound, boolean isModelKnown, int environmentPolicyCount, int minimalPlayerEntitiesInGameCount, List<PolicyCategoryInfo> policyCategoryInfoList, PolicyShuffleStrategy policyShuffleStrategy, boolean[][] graphMatrix, int attackLength, boolean[] isTargetNode) {
        super(maximalStepCountBound, isModelKnown, environmentPolicyCount, minimalPlayerEntitiesInGameCount, policyCategoryInfoList, policyShuffleStrategy);
        this.graphMatrix = graphMatrix;
        this.attackLength = attackLength;
        this.isTargetNode = isTargetNode;
    }

    @Override
    public String toLog() {
        return null;
    }

    @Override
    public String toFile() {
        return null;
    }
}
