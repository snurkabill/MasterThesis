package vahy.environment.agent.policy.exhaustive;

import vahy.api.model.State;
import vahy.api.search.simulation.NodeEvaluationSimulator;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.ActionType;
import vahy.environment.agent.policy.AbstractTreeSearchPolicy;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.node.nodeMetadata.empty.EmptySearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.empty.EmptyStateActionMetadata;
import vahy.impl.search.nodeSelector.exhaustive.BfsNodeSelector;

import java.util.LinkedHashMap;
import java.util.SplittableRandom;

public class BfsPolicy extends AbstractTreeSearchPolicy<EmptyStateActionMetadata<DoubleScalarReward>, EmptySearchNodeMetadata<ActionType, DoubleScalarReward>> {

    public BfsPolicy(
        SplittableRandom random,
        int uprateTreeCount,
        ImmutableStateImpl gameState,
        NodeTransitionUpdater<
                    ActionType,
                    DoubleScalarReward,
                    EmptyStateActionMetadata<DoubleScalarReward>,
                    EmptySearchNodeMetadata<ActionType, DoubleScalarReward>> nodeTransitionUpdater,
        NodeEvaluationSimulator<
                    ActionType,
                    DoubleScalarReward,
                    DoubleVectorialObservation,
                    EmptyStateActionMetadata<DoubleScalarReward>,
                    EmptySearchNodeMetadata<ActionType, DoubleScalarReward>,
                    State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> rewardSimulator) {
        super(random,
            uprateTreeCount,
            new SearchNodeBaseFactoryImpl<>(
                (stateRewardReturn, parent) -> {
                    Double cumulativeReward = parent != null ? parent.getSearchNodeMetadata().getCumulativeReward().getValue() : 0.0;
                    return new EmptySearchNodeMetadata<>(new DoubleScalarReward(stateRewardReturn.getReward().getValue() + cumulativeReward), new LinkedHashMap<>());
                    }
                ),
            BfsNodeSelector::new,
            stateRewardReturn -> new EmptyStateActionMetadata<>(stateRewardReturn.getReward()), nodeTransitionUpdater,
            gameState,
            rewardSimulator);
    }
}
