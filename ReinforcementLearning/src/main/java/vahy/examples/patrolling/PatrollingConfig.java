package vahy.examples.patrolling;

import vahy.api.episode.PolicyCategoryInfo;
import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.ProblemConfig;

import java.util.List;

public class PatrollingConfig extends ProblemConfig {

    private final boolean[][] graph;
    private final int attackLength;

    protected PatrollingConfig(int maximalStepCountBound, boolean isModelKnown, int environmentPolicyCount, int minimalPlayerEntitiesInGameCount, List<PolicyCategoryInfo> policyCategoryInfoList, PolicyShuffleStrategy policyShuffleStrategy, boolean[][] graph, int attackLength) {
        super(maximalStepCountBound, isModelKnown, environmentPolicyCount, minimalPlayerEntitiesInGameCount, policyCategoryInfoList, policyShuffleStrategy);
        this.graph = graph;
        this.attackLength = attackLength;
    }

    public boolean[][] getGraph() {
        return graph;
    }

    public int getAttackLength() {
        return attackLength;
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
