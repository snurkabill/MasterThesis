package vahy.examples.patrolling;

import vahy.api.episode.PolicyCategoryInfo;
import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.ProblemConfig;

import java.util.List;

public class PatrollingConfig extends ProblemConfig {

    private final GraphDef graph;

    protected PatrollingConfig(int maximalStepCountBound, boolean isModelKnown, int environmentPolicyCount, int minimalPlayerEntitiesInGameCount, List<PolicyCategoryInfo> policyCategoryInfoList, PolicyShuffleStrategy policyShuffleStrategy, GraphDef graph) {
        super(maximalStepCountBound, isModelKnown, environmentPolicyCount, minimalPlayerEntitiesInGameCount, policyCategoryInfoList, policyShuffleStrategy);
        this.graph = graph;
    }

    public GraphDef getGraph() {
        return graph;
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
