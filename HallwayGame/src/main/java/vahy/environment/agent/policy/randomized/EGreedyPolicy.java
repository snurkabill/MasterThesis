package vahy.environment.agent.policy.randomized;


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
import vahy.impl.search.node.nodeMetadata.AbstractSearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.AbstractStateActionMetadata;
import vahy.impl.search.node.nodeMetadata.empty.EmptySearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.empty.EmptyStateActionMetadata;
import vahy.impl.search.nodeExpander.BaseNodeExpander;
import vahy.impl.search.nodeSelector.treeTraversing.EGreedyNodeSelector;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.impl.search.update.TraversingTreeUpdater;

import java.util.LinkedHashMap;
import java.util.SplittableRandom;

public class EGreedyPolicy extends AbstractTreeSearchPolicy<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation, AbstractStateActionMetadata<DoubleScalarRewardDouble>, AbstractSearchNodeMetadata<ActionType, DoubleScalarRewardDouble, AbstractStateActionMetadata<DoubleScalarRewardDouble>>>  {

    public EGreedyPolicy(
        double epsilon,
        SplittableRandom random,
        int uprateTreeCount,
        ImmutableStateImpl gameState,
        NodeTransitionUpdater<
            ActionType,
            DoubleScalarRewardDouble,
            DoubleVectorialObservation,
            AbstractStateActionMetadata<DoubleScalarRewardDouble>,
            AbstractSearchNodeMetadata<ActionType, DoubleScalarRewardDouble, AbstractStateActionMetadata<DoubleScalarRewardDouble>>,
            State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> nodeTransitionUpdater,
        NodeEvaluationSimulator<
            ActionType,
            DoubleScalarRewardDouble,
            DoubleVectorialObservation,
            AbstractStateActionMetadata<DoubleScalarRewardDouble>, AbstractSearchNodeMetadata<ActionType, DoubleScalarRewardDouble, AbstractStateActionMetadata<DoubleScalarRewardDouble>>,
            State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> rewardSimulator) {
        super(random, uprateTreeCount, createSearchTree(epsilon, random, gameState, nodeTransitionUpdater, rewardSimulator));
    }

    private static SearchTreeImpl<
        ActionType,
        DoubleScalarRewardDouble,
        DoubleVectorialObservation,
        AbstractStateActionMetadata<DoubleScalarRewardDouble>,
        AbstractSearchNodeMetadata<ActionType, DoubleScalarRewardDouble, AbstractStateActionMetadata<DoubleScalarRewardDouble>>,
        State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> createSearchTree(
            double epsilon,
            SplittableRandom random,
            ImmutableStateImpl gameState,
            NodeTransitionUpdater<
                ActionType,
                DoubleScalarRewardDouble,
                DoubleVectorialObservation,
                AbstractStateActionMetadata<DoubleScalarRewardDouble>,
                AbstractSearchNodeMetadata<ActionType, DoubleScalarRewardDouble, AbstractStateActionMetadata<DoubleScalarRewardDouble>>,
                State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> nodeTransitionUpdater,
            NodeEvaluationSimulator<
                ActionType,
                DoubleScalarRewardDouble,
                DoubleVectorialObservation,
                AbstractStateActionMetadata<DoubleScalarRewardDouble>, AbstractSearchNodeMetadata<ActionType, DoubleScalarRewardDouble, AbstractStateActionMetadata<DoubleScalarRewardDouble>>,
                State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> rewardSimulator) {

        SearchNodeFactory<
            ActionType,
            DoubleScalarRewardDouble,
            DoubleVectorialObservation,
            AbstractStateActionMetadata<DoubleScalarRewardDouble>,
            AbstractSearchNodeMetadata<ActionType, DoubleScalarRewardDouble, AbstractStateActionMetadata<DoubleScalarRewardDouble>>,
            State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> searchNodeFactory = new SearchNodeBaseFactoryImpl<>(
                (stateRewardReturn, parent) -> {
                    Double cumulativeReward = parent != null ? parent.getSearchNodeMetadata().getCumulativeReward().getValue() : 0.0;
                    return new EmptySearchNodeMetadata<>(new DoubleScalarRewardDouble(stateRewardReturn.getReward().getValue() + cumulativeReward), new LinkedHashMap<>());
                });

        SearchNode<
            ActionType,
            DoubleScalarRewardDouble,
            DoubleVectorialObservation,
            AbstractStateActionMetadata<DoubleScalarRewardDouble>,
            AbstractSearchNodeMetadata<ActionType, DoubleScalarRewardDouble, AbstractStateActionMetadata<DoubleScalarRewardDouble>>,
            State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> root = searchNodeFactory.createNode(new ImmutableStateRewardReturnTuple<>(gameState, new DoubleScalarRewardDouble(0.0)), null, null);

        return new SearchTreeImpl<>(
            root,
            new EGreedyNodeSelector<>(epsilon, random),
            new BaseNodeExpander<>(searchNodeFactory, x -> new EmptyStateActionMetadata<>(x.getReward())),
            new TraversingTreeUpdater<>(nodeTransitionUpdater),
            rewardSimulator);
    }

}
