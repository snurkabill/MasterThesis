package vahy.environment.agent.policy.player.randomized;


import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.HallwayAction;
import vahy.environment.state.ImmutableStateImpl;
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

public class EGreedyPolicy extends AbstractEstimatedRewardMaximizingTreeSearchPolicy<HallwayAction, DoubleReward, DoubleVector, BaseSearchNodeMetadata<DoubleReward>, ImmutableStateImpl> {

    public EGreedyPolicy(
        SplittableRandom random,
        TreeUpdateCondition uprateTreeCount,
        ImmutableStateImpl gameState,
        NodeTransitionUpdater<
            HallwayAction,
            DoubleReward,
            DoubleVector,
            BaseSearchNodeMetadata<DoubleReward>,
            ImmutableStateImpl> nodeTransitionUpdater,
        NodeEvaluator<
            HallwayAction,
            DoubleReward,
            DoubleVector,
                    BaseSearchNodeMetadata<DoubleReward>,
                    ImmutableStateImpl> rewardSimulator) {
        super(random, uprateTreeCount, createSearchTree(random, gameState, nodeTransitionUpdater, rewardSimulator));
    }

    private static SearchTreeImpl<HallwayAction, DoubleReward, DoubleVector, BaseSearchNodeMetadata<DoubleReward>, ImmutableStateImpl> createSearchTree(
        SplittableRandom random,
        ImmutableStateImpl gameState,
        NodeTransitionUpdater<HallwayAction, DoubleReward, DoubleVector, BaseSearchNodeMetadata<DoubleReward>, ImmutableStateImpl> nodeTransitionUpdater,
        NodeEvaluator<HallwayAction, DoubleReward, DoubleVector, BaseSearchNodeMetadata<DoubleReward>, ImmutableStateImpl> nodeEvaluator)
    {
        SearchNodeFactory<HallwayAction, DoubleReward, DoubleVector, BaseSearchNodeMetadata<DoubleReward>, ImmutableStateImpl> searchNodeFactory =
            new SearchNodeBaseFactoryImpl<>(
                new BaseSearchNodeMetadataFactory<HallwayAction, DoubleReward, DoubleVector, ImmutableStateImpl>(new DoubleScalarRewardAggregator()
                )
            );

        SearchNode<HallwayAction, DoubleReward, DoubleVector, BaseSearchNodeMetadata<DoubleReward>, ImmutableStateImpl> root = searchNodeFactory.createNode(
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
