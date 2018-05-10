package vahy.environment.agent.policy.smart;


import vahy.api.model.State;
import vahy.api.search.simulation.NodeEvaluationSimulator;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.ActionType;
import vahy.environment.agent.policy.AbstractTreeSearchPolicy;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1SearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1StateActionMetadata;
import vahy.impl.search.nodeSelector.treeTraversing.UCB1NodeSelector;

import java.util.LinkedHashMap;
import java.util.SplittableRandom;

public class Ucb1Policy extends AbstractTreeSearchPolicy<Ucb1StateActionMetadata<DoubleScalarReward>, Ucb1SearchNodeMetadata<ActionType, DoubleScalarReward>> {

    public Ucb1Policy(
        SplittableRandom random,
        int uprateTreeCount,
        ImmutableStateImpl gameState,
        NodeTransitionUpdater<
            ActionType,
            DoubleScalarReward,
            Ucb1StateActionMetadata<DoubleScalarReward>,
            Ucb1SearchNodeMetadata<ActionType, DoubleScalarReward>> nodeTransitionUpdater,
        NodeEvaluationSimulator<
            ActionType,
            DoubleScalarReward,
            DoubleVectorialObservation,
            Ucb1StateActionMetadata<DoubleScalarReward>,
            Ucb1SearchNodeMetadata<ActionType, DoubleScalarReward>,
            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> rewardSimulator) {
        super(random,
            uprateTreeCount,
            new SearchNodeBaseFactoryImpl<>(
                (stateRewardReturn, parent) -> {
                    Double cumulativeReward = parent != null ? parent.getSearchNodeMetadata().getCumulativeReward().getValue() : 0.0;
                    return new Ucb1SearchNodeMetadata<>(new DoubleScalarReward(stateRewardReturn.getReward().getValue() + cumulativeReward), new LinkedHashMap<>());
                }
            ),
            () -> new UCB1NodeSelector<>(random, 1.0),
            stateRewardReturn -> new Ucb1StateActionMetadata<>(stateRewardReturn.getReward()), nodeTransitionUpdater,
            gameState,
            rewardSimulator);
    }
}
