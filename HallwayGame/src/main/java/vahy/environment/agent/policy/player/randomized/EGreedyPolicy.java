package vahy.environment.agent.policy.player.randomized;


import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.HallwayAction;
import vahy.environment.state.EnvironmentProbabilities;
import vahy.environment.state.HallwayStateImpl;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.policy.maximizingEstimatedReward.AbstractEstimatedRewardMaximizingTreeSearchPolicy;
import vahy.impl.search.node.factory.BaseSearchNodeMetadataFactory;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.node.nodeMetadata.BaseSearchNodeMetadata;
import vahy.impl.search.nodeSelector.treeTraversing.EGreedyNodeSelector;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.impl.search.update.TraversingTreeUpdater;

import java.util.SplittableRandom;

public class EGreedyPolicy extends AbstractEstimatedRewardMaximizingTreeSearchPolicy<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, BaseSearchNodeMetadata<DoubleReward>, HallwayStateImpl> {

    public EGreedyPolicy(
        SplittableRandom random,
        TreeUpdateCondition uprateTreeCount,
        HallwayStateImpl gameState,
        NodeTransitionUpdater<
            HallwayAction,
            DoubleReward,
            DoubleVector,
            EnvironmentProbabilities,
            BaseSearchNodeMetadata<DoubleReward>,
            HallwayStateImpl> nodeTransitionUpdater,
        NodeEvaluator<
            HallwayAction,
            DoubleReward,
            DoubleVector,
            EnvironmentProbabilities,
            BaseSearchNodeMetadata<DoubleReward>,
            HallwayStateImpl> rewardSimulator) {
        super(random, uprateTreeCount, createSearchTree(random, gameState, nodeTransitionUpdater, rewardSimulator));
    }

    private static SearchTreeImpl<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, BaseSearchNodeMetadata<DoubleReward>, HallwayStateImpl> createSearchTree(
        SplittableRandom random,
        HallwayStateImpl gameState,
        NodeTransitionUpdater<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, BaseSearchNodeMetadata<DoubleReward>, HallwayStateImpl> nodeTransitionUpdater,
        NodeEvaluator<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, BaseSearchNodeMetadata<DoubleReward>, HallwayStateImpl> nodeEvaluator)
    {
        SearchNodeFactory<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, BaseSearchNodeMetadata<DoubleReward>, HallwayStateImpl> searchNodeFactory =
            new SearchNodeBaseFactoryImpl<>(
                new BaseSearchNodeMetadataFactory<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, HallwayStateImpl>(new DoubleScalarRewardAggregator()
                )
            );

        SearchNode<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, BaseSearchNodeMetadata<DoubleReward>, HallwayStateImpl> root = searchNodeFactory.createNode(
            new ImmutableStateRewardReturnTuple<>(gameState, new DoubleReward(0.0)),
            null,
            null);

        return new SearchTreeImpl<>(
            root,
            new EGreedyNodeSelector<>(random, 1.0),
            new TraversingTreeUpdater<>(nodeTransitionUpdater),
            nodeEvaluator);
    }

}
