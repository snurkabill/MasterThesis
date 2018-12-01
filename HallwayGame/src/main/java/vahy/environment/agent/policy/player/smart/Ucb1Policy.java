package vahy.environment.agent.policy.player.smart;


import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
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
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.impl.search.MCTS.MonteCarloTreeSearchUpdater;
import vahy.search.MinMaxNormalizingNodeSelector;

import java.util.SplittableRandom;

public class Ucb1Policy extends AbstractEstimatedRewardMaximizingTreeSearchPolicy<ActionType, DoubleReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleReward>, ImmutableStateImpl> {

    public Ucb1Policy(
        SplittableRandom random,
        TreeUpdateCondition uprateTreeCondition,
        double explorationConstant,
        ImmutableStateImpl gameState,
        NodeEvaluator<
                    ActionType,
            DoubleReward,
                    DoubleVectorialObservation,
            MonteCarloTreeSearchMetadata<DoubleReward>,
                    ImmutableStateImpl> rewardSimulator) {
        super(random, uprateTreeCondition, createSearchTree(random, gameState, rewardSimulator, explorationConstant));
    }

    private static SearchTreeImpl<ActionType, DoubleReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleReward>, ImmutableStateImpl> createSearchTree(
        SplittableRandom random,
        ImmutableStateImpl gameState,
        NodeEvaluator<ActionType, DoubleReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleReward>, ImmutableStateImpl> nodeEvaluator,
        double explorationConstant)
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
            new MinMaxNormalizingNodeSelector(random, explorationConstant),
            new MonteCarloTreeSearchUpdater<>(),
            nodeEvaluator);
    }
}
