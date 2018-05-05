package vahy.environment.agent.policy.randomized;

import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.ActionType;
import vahy.environment.agent.policy.AbstractTreeSearchPolicy;
import vahy.impl.model.DoubleScalarReward;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.node.nodeMetadata.empty.EmptySearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.empty.EmptyStateActionMetadata;
import vahy.impl.search.nodeSelector.treeTraversing.EGreedy;

import java.util.LinkedHashMap;
import java.util.SplittableRandom;

public class EGreedyPolicy extends AbstractTreeSearchPolicy<EmptyStateActionMetadata<DoubleScalarReward>, EmptySearchNodeMetadata<ActionType, DoubleScalarReward>> {


    public EGreedyPolicy(
        SplittableRandom random,
        int uprateTreeCount,
        double epsilon,
        NodeTransitionUpdater<
            ActionType,
            DoubleScalarReward,
            EmptyStateActionMetadata<DoubleScalarReward>,
            EmptySearchNodeMetadata<ActionType, DoubleScalarReward>> nodeTransitionUpdater) {
        super(random,
            uprateTreeCount,
            new SearchNodeBaseFactoryImpl<>(
                (stateRewardReturn, parent) -> {
                    Double cumulativeReward = parent != null ? parent.getSearchNodeMetadata().getCumulativeReward().getValue() : 0.0;
                    return new EmptySearchNodeMetadata<>(new DoubleScalarReward(stateRewardReturn.getReward().getValue() + cumulativeReward), new LinkedHashMap<>());
                }
            ),
            () -> new EGreedy<>(epsilon, random),
            stateRewardReturn -> new EmptyStateActionMetadata<>(stateRewardReturn.getReward()), nodeTransitionUpdater);
    }
}
