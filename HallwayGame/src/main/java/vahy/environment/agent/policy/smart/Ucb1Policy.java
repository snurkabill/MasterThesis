package vahy.environment.agent.policy.smart;


import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.simulation.NodeEvaluationSimulator;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.reward.DoubleScalarRewardDouble;
import vahy.impl.policy.AbstractTreeSearchPolicy;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1SearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1StateActionMetadata;
import vahy.impl.search.nodeExpander.BaseNodeExpander;
import vahy.impl.search.nodeSelector.treeTraversing.ucb1.Ucb1MinMaxExplorationConstantNodeSelector;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.impl.search.update.TraversingTreeUpdater;

import java.util.LinkedHashMap;
import java.util.SplittableRandom;

public class Ucb1Policy extends AbstractTreeSearchPolicy<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation, Ucb1StateActionMetadata<DoubleScalarRewardDouble>, Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>>   {

    public Ucb1Policy(
        SplittableRandom random,
        int uprateTreeCount,
        ImmutableStateImpl gameState,
        NodeTransitionUpdater<
            ActionType,
            DoubleScalarRewardDouble,
            DoubleVectorialObservation,
            Ucb1StateActionMetadata<DoubleScalarRewardDouble>,
            Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>,
            State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> nodeTransitionUpdater,
        NodeEvaluationSimulator<
            ActionType,
            DoubleScalarRewardDouble,
            DoubleVectorialObservation,
            Ucb1StateActionMetadata<DoubleScalarRewardDouble>,
            Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>,
            State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> rewardSimulator) {
        super(random, uprateTreeCount, createSearchTree(random, gameState, nodeTransitionUpdater, rewardSimulator));
    }

    private static SearchTreeImpl<
        ActionType,
        DoubleScalarRewardDouble,
        DoubleVectorialObservation,
        Ucb1StateActionMetadata<DoubleScalarRewardDouble>,
        Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>,
        State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> createSearchTree(
        SplittableRandom random,
        ImmutableStateImpl gameState,
        NodeTransitionUpdater<
            ActionType,
            DoubleScalarRewardDouble,
            DoubleVectorialObservation,
            Ucb1StateActionMetadata<DoubleScalarRewardDouble>,
            Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>,
            State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> nodeTransitionUpdater,
        NodeEvaluationSimulator<
            ActionType,
            DoubleScalarRewardDouble,
            DoubleVectorialObservation,
            Ucb1StateActionMetadata<DoubleScalarRewardDouble>,
            Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>,
            State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> rewardSimulator) {

        SearchNodeFactory<
            ActionType,
            DoubleScalarRewardDouble,
            DoubleVectorialObservation,
            Ucb1StateActionMetadata<DoubleScalarRewardDouble>,
            Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>,
            State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> searchNodeFactory = new SearchNodeBaseFactoryImpl<>(
                (stateRewardReturn, parent) -> {
                    Double cumulativeReward = parent != null ? parent.getSearchNodeMetadata().getCumulativeReward().getValue() : 0.0;
                    return new Ucb1SearchNodeMetadata<>(new DoubleScalarRewardDouble(stateRewardReturn.getReward().getValue() + cumulativeReward), new LinkedHashMap<>());
                });

        SearchNode<
            ActionType,
            DoubleScalarRewardDouble,
            DoubleVectorialObservation,
            Ucb1StateActionMetadata<DoubleScalarRewardDouble>,
            Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>,
            State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> root = searchNodeFactory.createNode(new ImmutableStateRewardReturnTuple<>(gameState, new DoubleScalarRewardDouble(0.0)), null, null);

        return new SearchTreeImpl<>(
            root,
            new Ucb1MinMaxExplorationConstantNodeSelector<>(random, 1.0),
            new BaseNodeExpander<>(searchNodeFactory, x -> new Ucb1StateActionMetadata<>(x.getReward())),
            new TraversingTreeUpdater<>(nodeTransitionUpdater),
            rewardSimulator);
    }
}
