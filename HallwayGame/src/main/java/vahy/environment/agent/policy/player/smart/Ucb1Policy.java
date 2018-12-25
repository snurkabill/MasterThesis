package vahy.environment.agent.policy.player.smart;


import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.environment.HallwayAction;
import vahy.environment.state.EnvironmentProbabilities;
import vahy.environment.state.HallwayStateImpl;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVector;
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

public class Ucb1Policy extends AbstractEstimatedRewardMaximizingTreeSearchPolicy<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, MonteCarloTreeSearchMetadata<DoubleReward>, HallwayStateImpl> {

    public Ucb1Policy(
        SplittableRandom random,
        TreeUpdateCondition uprateTreeCondition,
        double explorationConstant,
        HallwayStateImpl gameState,
        NodeEvaluator<
            HallwayAction,
            DoubleReward,
            DoubleVector,
            EnvironmentProbabilities,
            MonteCarloTreeSearchMetadata<DoubleReward>,
            HallwayStateImpl> rewardSimulator) {
        super(random, uprateTreeCondition, createSearchTree(random, gameState, rewardSimulator, explorationConstant));
    }

    private static SearchTreeImpl<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, MonteCarloTreeSearchMetadata<DoubleReward>, HallwayStateImpl> createSearchTree(
        SplittableRandom random,
        HallwayStateImpl gameState,
        NodeEvaluator<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, MonteCarloTreeSearchMetadata<DoubleReward>, HallwayStateImpl> nodeEvaluator,
        double explorationConstant)
    {
        SearchNodeFactory<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, MonteCarloTreeSearchMetadata<DoubleReward>, HallwayStateImpl> searchNodeFactory =
            new SearchNodeBaseFactoryImpl<>(
                new MonteCarloTreeSearchMetadataFactory<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, HallwayStateImpl>(new DoubleScalarRewardAggregator()
                )
        );

        SearchNode<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, MonteCarloTreeSearchMetadata<DoubleReward>, HallwayStateImpl> root = searchNodeFactory.createNode(
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
