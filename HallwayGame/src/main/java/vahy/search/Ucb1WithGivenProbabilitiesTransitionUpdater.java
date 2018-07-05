package vahy.search;

import vahy.api.model.State;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.ActionType;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarRewardDouble;
import vahy.impl.search.node.nodeMetadata.AbstractStateActionMetadata;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1SearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1StateActionMetadata;

import java.util.Map;
import java.util.stream.Collectors;

public class Ucb1WithGivenProbabilitiesTransitionUpdater extends MaximizingRewardGivenProbabilities implements NodeTransitionUpdater<
    ActionType,
    DoubleScalarRewardDouble,
    DoubleVectorialObservation,
    Ucb1StateActionMetadata<DoubleScalarRewardDouble>,
    Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>,
    State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> {

    private final double discountFactor;
    private final RewardAggregator<DoubleScalarRewardDouble> rewardAggregator;

    public Ucb1WithGivenProbabilitiesTransitionUpdater(double discountFactor, RewardAggregator<DoubleScalarRewardDouble> rewardAggregator) {
        this.discountFactor = discountFactor;
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public void applyUpdate(SearchNode<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation, Ucb1StateActionMetadata<DoubleScalarRewardDouble>, Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>, State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> parent, SearchNode<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation, Ucb1StateActionMetadata<DoubleScalarRewardDouble>, Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>, State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> child, ActionType action) {
        Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble> parentSearchNodeMetadata = parent.getSearchNodeMetadata();
        Ucb1StateActionMetadata<DoubleScalarRewardDouble> stateActionMetadata = parentSearchNodeMetadata.getStateActionMetadataMap().get(action);

        stateActionMetadata.setEstimatedTotalReward(new DoubleScalarRewardDouble(rewardAggregator.aggregateDiscount(
            stateActionMetadata.getGainedReward(),
            child.getSearchNodeMetadata().getEstimatedTotalReward(),
            discountFactor).getValue()));
        double parentCumulativeEstimates = parentSearchNodeMetadata.getEstimatedTotalReward().getValue() * (parentSearchNodeMetadata.getVisitCounter() - 1);

        DoubleScalarRewardDouble newParentCumulativeEstimate = resolveReward(
            parent.getWrappedState(),
            parent.getSearchNodeMetadata()
                .getStateActionMetadataMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    o -> (AbstractStateActionMetadata<DoubleScalarRewardDouble>) o.getValue()
                ))
        );

        double sum = parentCumulativeEstimates + newParentCumulativeEstimate.getValue();
        parentSearchNodeMetadata.setEstimatedTotalReward(new DoubleScalarRewardDouble(sum / parentSearchNodeMetadata.getVisitCounter()));
    }

}
