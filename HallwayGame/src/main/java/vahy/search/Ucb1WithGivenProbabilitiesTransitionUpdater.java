package vahy.search;

import vahy.api.model.State;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.ActionType;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.node.nodeMetadata.MCTSNodeMetadata;

import java.util.Map;
import java.util.stream.Collectors;

public class Ucb1WithGivenProbabilitiesTransitionUpdater extends MaximizingRewardGivenProbabilities implements NodeTransitionUpdater<
    ActionType,
    DoubleScalarReward,
    DoubleVectorialObservation,
    Ucb1StateActionMetadata<DoubleScalarReward>,
    MCTSNodeMetadata<ActionType, DoubleScalarReward>,
    State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> {

    private final double discountFactor;
    private final RewardAggregator<DoubleScalarReward> rewardAggregator;

    public Ucb1WithGivenProbabilitiesTransitionUpdater(double discountFactor, RewardAggregator<DoubleScalarReward> rewardAggregator) {
        this.discountFactor = discountFactor;
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public void applyUpdate(SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, Ucb1StateActionMetadata<DoubleScalarReward>, MCTSNodeMetadata<ActionType, DoubleScalarReward>, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> parent, SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, Ucb1StateActionMetadata<DoubleScalarReward>, MCTSNodeMetadata<ActionType, DoubleScalarReward>, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> child, ActionType action) {
        MCTSNodeMetadata<ActionType, DoubleScalarReward> parentSearchNodeMetadata = parent.getSearchNodeMetadata();
        Ucb1StateActionMetadata<DoubleScalarReward> stateActionMetadata = parentSearchNodeMetadata.getStateActionMetadataMap().get(action);

        stateActionMetadata.setEstimatedTotalReward(new DoubleScalarReward(rewardAggregator.aggregateDiscount(
            stateActionMetadata.getGainedReward(),
            child.getSearchNodeMetadata().getEstimatedTotalReward(),
            discountFactor).getValue()));
        double parentCumulativeEstimates = parentSearchNodeMetadata.getEstimatedTotalReward().getValue() * (parentSearchNodeMetadata.getVisitCounter() - 1);

        DoubleScalarReward newParentCumulativeEstimate = resolveReward(
            parent.getWrappedState(),
            parent.getSearchNodeMetadata()
                .getStateActionMetadataMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    o -> (AbstractStateActionMetadata<DoubleScalarReward>) o.getValue()
                ))
        );

        double sum = parentCumulativeEstimates + newParentCumulativeEstimate.getValue();
        parentSearchNodeMetadata.setEstimatedTotalReward(new DoubleScalarReward(sum / parentSearchNodeMetadata.getVisitCounter()));
    }

}
