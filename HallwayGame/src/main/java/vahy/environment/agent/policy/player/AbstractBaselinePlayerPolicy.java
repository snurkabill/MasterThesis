package vahy.environment.agent.policy.player;

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
import vahy.impl.search.node.factory.MCTSSearchNodeMetadataFactory;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.node.nodeMetadata.MCTSNodeMetadata;
import vahy.impl.search.nodeSelector.treeTraversing.ucb1.Ucb1NodeSelector;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.impl.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.impl.search.update.TraversingTreeUpdater;

import java.util.SplittableRandom;

public class AbstractBaselinePlayerPolicy extends AbstractEstimatedRewardMaximizingTreeSearchPolicy<ActionType, DoubleScalarReward, DoubleVectorialObservation, MCTSNodeMetadata<DoubleScalarReward>, ImmutableStateImpl> {

    public AbstractBaselinePlayerPolicy(
        SplittableRandom random,
        TreeUpdateCondition uprateTreeCount,
        ImmutableStateImpl gameState,
        NodeTransitionUpdater<
                    ActionType,
                    DoubleScalarReward,
                    DoubleVectorialObservation,
                    MCTSNodeMetadata<DoubleScalarReward>,
                    ImmutableStateImpl> nodeTransitionUpdater,
        NodeEvaluator<
                    ActionType,
                    DoubleScalarReward,
                    DoubleVectorialObservation,
                    MCTSNodeMetadata<DoubleScalarReward>,
                    ImmutableStateImpl> rewardSimulator) {
        super(random, uprateTreeCount, createSearchTree(random, gameState, nodeTransitionUpdater, rewardSimulator));
    }

    private static SearchTreeImpl<ActionType, DoubleScalarReward, DoubleVectorialObservation, MCTSNodeMetadata<DoubleScalarReward>, ImmutableStateImpl> createSearchTree(
        SplittableRandom random,
        ImmutableStateImpl gameState,
        NodeTransitionUpdater<ActionType, DoubleScalarReward, DoubleVectorialObservation, MCTSNodeMetadata<DoubleScalarReward>, ImmutableStateImpl> nodeTransitionUpdater,
        NodeEvaluator<ActionType, DoubleScalarReward, DoubleVectorialObservation, MCTSNodeMetadata<DoubleScalarReward>, ImmutableStateImpl> nodeEvaluator)
    {
        SearchNodeFactory<ActionType, DoubleScalarReward, DoubleVectorialObservation, MCTSNodeMetadata<DoubleScalarReward>, ImmutableStateImpl> searchNodeFactory =
            new SearchNodeBaseFactoryImpl<>(
                new MCTSSearchNodeMetadataFactory<ActionType, DoubleScalarReward, DoubleVectorialObservation, ImmutableStateImpl>(new DoubleScalarRewardAggregator()
                )
            );

        SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, MCTSNodeMetadata<DoubleScalarReward>, ImmutableStateImpl> root = searchNodeFactory.createNode(
            new ImmutableStateRewardReturnTuple<>(gameState, new DoubleScalarReward(0.0)),
            null,
            null);

        return new SearchTreeImpl<>(
            root,
            new Ucb1NodeSelector<>(random, 1.0),
            new TraversingTreeUpdater<>(nodeTransitionUpdater),
            nodeEvaluator);
    }
}
