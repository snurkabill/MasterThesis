package vahy.environment.agent.policy.player;

import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.policy.maximizingEstimatedReward.AbstractEstimatedRewardMaximizingTreeSearchPolicy;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadata;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadataFactory;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.MCTS.ucb1.Ucb1NodeSelector;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.impl.search.update.TraversingTreeUpdater;

import java.util.SplittableRandom;

public class AbstractBaselinePlayerPolicy extends AbstractEstimatedRewardMaximizingTreeSearchPolicy<ActionType, DoubleReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleReward>, ImmutableStateImpl> {

    public AbstractBaselinePlayerPolicy(
        SplittableRandom random,
        TreeUpdateCondition uprateTreeCount,
        ImmutableStateImpl gameState,
        NodeTransitionUpdater<
                    ActionType,
            DoubleReward,
                    DoubleVectorialObservation,
            MonteCarloTreeSearchMetadata<DoubleReward>,
                    ImmutableStateImpl> nodeTransitionUpdater,
        NodeEvaluator<
                    ActionType,
            DoubleReward,
                    DoubleVectorialObservation,
            MonteCarloTreeSearchMetadata<DoubleReward>,
                    ImmutableStateImpl> rewardSimulator) {
        super(random, uprateTreeCount, createSearchTree(random, gameState, nodeTransitionUpdater, rewardSimulator));
    }

    private static SearchTreeImpl<ActionType, DoubleReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleReward>, ImmutableStateImpl> createSearchTree(
        SplittableRandom random,
        ImmutableStateImpl gameState,
        NodeTransitionUpdater<ActionType, DoubleReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleReward>, ImmutableStateImpl> nodeTransitionUpdater,
        NodeEvaluator<ActionType, DoubleReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleReward>, ImmutableStateImpl> nodeEvaluator)
    {
        SearchNodeFactory<ActionType, DoubleReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleReward>, ImmutableStateImpl> searchNodeFactory =
            new SearchNodeBaseFactoryImpl<>(
                new MonteCarloTreeSearchMetadataFactory<ActionType, DoubleReward, DoubleVectorialObservation, ImmutableStateImpl>(new DoubleScalarRewardAggregator()
                )
            );

        SearchNode<ActionType, DoubleReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleReward>, ImmutableStateImpl> root = searchNodeFactory.createNode(
            new ImmutableStateRewardReturnTuple<>(gameState, new DoubleReward(0.0)),
            null,
            null);

        return new SearchTreeImpl<>(
            root,
            new Ucb1NodeSelector<>(random, 1.0),
            new TraversingTreeUpdater<>(nodeTransitionUpdater),
            nodeEvaluator);
    }
}
