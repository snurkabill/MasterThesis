package vahy.environment.agent.policy.player;

import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.HallwayAction;
import vahy.environment.state.EnvironmentProbabilities;
import vahy.environment.state.HallwayStateImpl;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVector;
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

public class AbstractBaselinePlayerPolicy extends AbstractEstimatedRewardMaximizingTreeSearchPolicy<HallwayAction,  DoubleVector, EnvironmentProbabilities, MonteCarloTreeSearchMetadata, HallwayStateImpl> {

    public AbstractBaselinePlayerPolicy(
        SplittableRandom random,
        TreeUpdateCondition uprateTreeCount,
        HallwayStateImpl gameState,
        NodeTransitionUpdater<
            HallwayAction,
            DoubleVector,
            EnvironmentProbabilities,
            MonteCarloTreeSearchMetadata,
            HallwayStateImpl> nodeTransitionUpdater,
        NodeEvaluator<
            HallwayAction,
            DoubleVector,
            EnvironmentProbabilities,
            MonteCarloTreeSearchMetadata,
            HallwayStateImpl> rewardSimulator) {
        super(random, uprateTreeCount, createSearchTree(random, gameState, nodeTransitionUpdater, rewardSimulator));
    }

    private static SearchTreeImpl<HallwayAction,  DoubleVector, EnvironmentProbabilities, MonteCarloTreeSearchMetadata, HallwayStateImpl> createSearchTree(
        SplittableRandom random,
        HallwayStateImpl gameState,
        NodeTransitionUpdater<HallwayAction,  DoubleVector, EnvironmentProbabilities, MonteCarloTreeSearchMetadata, HallwayStateImpl> nodeTransitionUpdater,
        NodeEvaluator<HallwayAction,  DoubleVector, EnvironmentProbabilities, MonteCarloTreeSearchMetadata, HallwayStateImpl> nodeEvaluator)
    {
        SearchNodeFactory<HallwayAction,  DoubleVector, EnvironmentProbabilities, MonteCarloTreeSearchMetadata, HallwayStateImpl> searchNodeFactory =
            new SearchNodeBaseFactoryImpl<>(
                new MonteCarloTreeSearchMetadataFactory<HallwayAction,  DoubleVector, EnvironmentProbabilities, HallwayStateImpl>(new DoubleScalarRewardAggregator()
                )
            );

        SearchNode<HallwayAction,  DoubleVector, EnvironmentProbabilities, MonteCarloTreeSearchMetadata, HallwayStateImpl> root = searchNodeFactory.createNode(
            new ImmutableStateRewardReturnTuple<>(gameState, 0.0),
            null,
            null);

        return new SearchTreeImpl<>(
            root,
            new Ucb1NodeSelector<>(random, 1.0),
            new TraversingTreeUpdater<>(nodeTransitionUpdater),
            nodeEvaluator);
    }
}
