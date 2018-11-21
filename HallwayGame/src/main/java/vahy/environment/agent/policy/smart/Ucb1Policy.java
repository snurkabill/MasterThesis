package vahy.environment.agent.policy.smart;


import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.api.search.simulation.NodeEvaluationSimulator;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.policy.maximizingEstimatedReward.AbstractEstimatedRewardMaximizingTreeSearchPolicy;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.node.nodeMetadata.MCTSNodeMetadata;
import vahy.impl.search.nodeExpander.BaseNodeExpander;
import vahy.impl.search.nodeSelector.treeTraversing.ucb1.Ucb1MinMaxExplorationConstantNodeSelector;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.impl.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.impl.search.update.TraversingTreeUpdater;

import java.util.LinkedHashMap;
import java.util.SplittableRandom;

public class Ucb1Policy extends AbstractEstimatedRewardMaximizingTreeSearchPolicy<ActionType, DoubleScalarReward, DoubleVectorialObservation, Ucb1StateActionMetadata<DoubleScalarReward>, MCTSNodeMetadata<ActionType, DoubleScalarReward>> {

    public Ucb1Policy(
        SplittableRandom random,
        TreeUpdateCondition uprateTreeCount,
        ImmutableStateImpl gameState,
        NodeTransitionUpdater<
            ActionType,
            DoubleScalarReward,
            DoubleVectorialObservation,
            Ucb1StateActionMetadata<DoubleScalarReward>,
            MCTSNodeMetadata<ActionType, DoubleScalarReward>,
            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> nodeTransitionUpdater,
        NodeEvaluationSimulator<
            ActionType,
            DoubleScalarReward,
            DoubleVectorialObservation,
            Ucb1StateActionMetadata<DoubleScalarReward>,
            MCTSNodeMetadata<ActionType, DoubleScalarReward>,
            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> rewardSimulator) {
        super(random, uprateTreeCount, createSearchTree(random, gameState, nodeTransitionUpdater, rewardSimulator));
    }

    private static SearchTreeImpl<
        ActionType,
        DoubleScalarReward,
        DoubleVectorialObservation,
        Ucb1StateActionMetadata<DoubleScalarReward>,
        MCTSNodeMetadata<ActionType, DoubleScalarReward>,
        State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> createSearchTree(
        SplittableRandom random,
        ImmutableStateImpl gameState,
        NodeTransitionUpdater<
            ActionType,
            DoubleScalarReward,
            DoubleVectorialObservation,
            Ucb1StateActionMetadata<DoubleScalarReward>,
            MCTSNodeMetadata<ActionType, DoubleScalarReward>,
            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> nodeTransitionUpdater,
        NodeEvaluationSimulator<
            ActionType,
            DoubleScalarReward,
            DoubleVectorialObservation,
            Ucb1StateActionMetadata<DoubleScalarReward>,
            MCTSNodeMetadata<ActionType, DoubleScalarReward>,
            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> rewardSimulator) {

        SearchNodeFactory<
            ActionType,
            DoubleScalarReward,
            DoubleVectorialObservation,
            Ucb1StateActionMetadata<DoubleScalarReward>,
            MCTSNodeMetadata<ActionType, DoubleScalarReward>,
            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> searchNodeFactory = new SearchNodeBaseFactoryImpl<>(
                (stateRewardReturn, parent) -> {
                    Double cumulativeReward = parent != null ? parent.getSearchNodeMetadata().getCumulativeReward().getValue() : 0.0;
                    return new MCTSNodeMetadata<>(new DoubleScalarReward(stateRewardReturn.getReward().getValue() + cumulativeReward), new LinkedHashMap<>());
                });

        SearchNode<
            ActionType,
            DoubleScalarReward,
            DoubleVectorialObservation,
            Ucb1StateActionMetadata<DoubleScalarReward>,
            MCTSNodeMetadata<ActionType, DoubleScalarReward>,
            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> root = searchNodeFactory.createNode(new ImmutableStateRewardReturnTuple<>(gameState, new DoubleScalarReward(0.0)), null, null);

        return new SearchTreeImpl<>(
            root,
            new Ucb1MinMaxExplorationConstantNodeSelector<>(random, 1.0),
            new BaseNodeExpander<>(searchNodeFactory, x -> new Ucb1StateActionMetadata<>(x.getReward())),
            new TraversingTreeUpdater<>(nodeTransitionUpdater),
            rewardSimulator);
    }
}
