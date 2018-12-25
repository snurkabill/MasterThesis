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

public class AbstractBaselinePlayerPolicy extends AbstractEstimatedRewardMaximizingTreeSearchPolicy<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, MonteCarloTreeSearchMetadata<DoubleReward>, HallwayStateImpl> {

    public AbstractBaselinePlayerPolicy(
        SplittableRandom random,
        TreeUpdateCondition uprateTreeCount,
        HallwayStateImpl gameState,
        NodeTransitionUpdater<
            HallwayAction,
            DoubleReward,
            DoubleVector,
            EnvironmentProbabilities,
            MonteCarloTreeSearchMetadata<DoubleReward>,
            HallwayStateImpl> nodeTransitionUpdater,
        NodeEvaluator<
            HallwayAction,
            DoubleReward,
            DoubleVector,
            EnvironmentProbabilities,
            MonteCarloTreeSearchMetadata<DoubleReward>,
            HallwayStateImpl> rewardSimulator) {
        super(random, uprateTreeCount, createSearchTree(random, gameState, nodeTransitionUpdater, rewardSimulator));
    }

    private static SearchTreeImpl<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, MonteCarloTreeSearchMetadata<DoubleReward>, HallwayStateImpl> createSearchTree(
        SplittableRandom random,
        HallwayStateImpl gameState,
        NodeTransitionUpdater<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, MonteCarloTreeSearchMetadata<DoubleReward>, HallwayStateImpl> nodeTransitionUpdater,
        NodeEvaluator<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, MonteCarloTreeSearchMetadata<DoubleReward>, HallwayStateImpl> nodeEvaluator)
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
            new Ucb1NodeSelector<>(random, 1.0),
            new TraversingTreeUpdater<>(nodeTransitionUpdater),
            nodeEvaluator);
    }
}
