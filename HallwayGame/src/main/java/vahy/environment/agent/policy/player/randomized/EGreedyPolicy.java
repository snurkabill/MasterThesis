package vahy.environment.agent.policy.player.randomized;


import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
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

public class EGreedyPolicy extends AbstractEstimatedRewardMaximizingTreeSearchPolicy<ActionType, DoubleScalarReward, DoubleVectorialObservation, BaseSearchNodeMetadata<DoubleScalarReward>, ImmutableStateImpl> {

    public EGreedyPolicy(
        SplittableRandom random,
        TreeUpdateCondition uprateTreeCount,
        ImmutableStateImpl gameState,
        NodeTransitionUpdater<
            ActionType,
            DoubleScalarReward,
            DoubleVectorialObservation,
            BaseSearchNodeMetadata<DoubleScalarReward>,
            ImmutableStateImpl> nodeTransitionUpdater,
        NodeEvaluator<
                    ActionType,
                    DoubleScalarReward,
                    DoubleVectorialObservation,
                    BaseSearchNodeMetadata<DoubleScalarReward>,
                    ImmutableStateImpl> rewardSimulator) {
        super(random, uprateTreeCount, createSearchTree(random, gameState, nodeTransitionUpdater, rewardSimulator));
    }

    private static SearchTreeImpl<ActionType, DoubleScalarReward, DoubleVectorialObservation, BaseSearchNodeMetadata<DoubleScalarReward>, ImmutableStateImpl> createSearchTree(
        SplittableRandom random,
        ImmutableStateImpl gameState,
        NodeTransitionUpdater<ActionType, DoubleScalarReward, DoubleVectorialObservation, BaseSearchNodeMetadata<DoubleScalarReward>, ImmutableStateImpl> nodeTransitionUpdater,
        NodeEvaluator<ActionType, DoubleScalarReward, DoubleVectorialObservation, BaseSearchNodeMetadata<DoubleScalarReward>, ImmutableStateImpl> nodeEvaluator)
    {
        SearchNodeFactory<ActionType, DoubleScalarReward, DoubleVectorialObservation, BaseSearchNodeMetadata<DoubleScalarReward>, ImmutableStateImpl> searchNodeFactory =
            new SearchNodeBaseFactoryImpl<>(
                new BaseSearchNodeMetadataFactory<ActionType, DoubleScalarReward, DoubleVectorialObservation, ImmutableStateImpl>(new DoubleScalarRewardAggregator()
                )
            );

        SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, BaseSearchNodeMetadata<DoubleScalarReward>, ImmutableStateImpl> root = searchNodeFactory.createNode(
            new ImmutableStateRewardReturnTuple<>(gameState, new DoubleScalarReward(0.0)),
            null,
            null);

        return new SearchTreeImpl<>(
            root,
            new EGreedyNodeSelector<>(random, 1.0),
            new TraversingTreeUpdater<>(nodeTransitionUpdater),
            nodeEvaluator);
    }

}
