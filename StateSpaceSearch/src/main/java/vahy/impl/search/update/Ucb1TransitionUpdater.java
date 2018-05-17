package vahy.impl.search.update;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1SearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1StateActionMetadata;

public class Ucb1TransitionUpdater<
    TAction extends Action,
    TObservation extends Observation,
    TState extends State<TAction, DoubleScalarReward, TObservation>>
    implements NodeTransitionUpdater<TAction, DoubleScalarReward, TObservation, Ucb1StateActionMetadata<DoubleScalarReward>, Ucb1SearchNodeMetadata<TAction, DoubleScalarReward>, TState> {

    private final double discountFactor;
    private final RewardAggregator<DoubleScalarReward> rewardAggregator;

    public Ucb1TransitionUpdater(double discountFactor, RewardAggregator<DoubleScalarReward> rewardAggregator) {
        this.discountFactor = discountFactor;
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public void applyUpdate(
        SearchNode<TAction, DoubleScalarReward, TObservation, Ucb1StateActionMetadata<DoubleScalarReward>, Ucb1SearchNodeMetadata<TAction, DoubleScalarReward>, TState> parent,
        SearchNode<TAction, DoubleScalarReward, TObservation, Ucb1StateActionMetadata<DoubleScalarReward>, Ucb1SearchNodeMetadata<TAction, DoubleScalarReward>, TState> child, TAction action) {
        Ucb1SearchNodeMetadata<TAction, DoubleScalarReward> parentSearchNodeMetadata = parent.getSearchNodeMetadata();
        Ucb1StateActionMetadata<DoubleScalarReward> stateActionMetadata = parentSearchNodeMetadata.getStateActionMetadataMap().get(action);

        stateActionMetadata.setEstimatedTotalReward(new DoubleScalarReward(rewardAggregator.aggregateDiscount(
            stateActionMetadata.getGainedReward(),
            child.getSearchNodeMetadata().getEstimatedTotalReward(),
            discountFactor).getValue()));
        double parentCumulativeEstimates = parentSearchNodeMetadata.getEstimatedTotalReward().getValue() * (parentSearchNodeMetadata.getVisitCounter() - 1);

        DoubleScalarReward newParentCumulativeEstimate = parent.isOpponentTurn() ?
            rewardAggregator.averageReward(parentSearchNodeMetadata
                .getStateActionMetadataMap()
                .values()
                .stream()
                .map(StateActionMetadata::getEstimatedTotalReward))
            : parentSearchNodeMetadata
                .getStateActionMetadataMap()
                .values()
                .stream()
                .map(StateActionMetadata::getEstimatedTotalReward)
                .max(Comparable::compareTo)
                .orElseThrow(() -> new IllegalStateException("Children should be always expanded when doing transition update"));

        double sum = parentCumulativeEstimates + newParentCumulativeEstimate.getValue();
        parentSearchNodeMetadata.setEstimatedTotalReward(new DoubleScalarReward(sum / parentSearchNodeMetadata.getVisitCounter()));

//        DoubleScalarReward cumulativeEstimates = rewardAggregator.aggregateDiscount(parentSearchNodeMetadata.getCumulativeEstimates(), child.getSearchNodeMetadata().getEstimatedTotalReward(), discountFactor);
//        parentSearchNodeMetadata.setCumulativeEstimates(cumulativeEstimates);
//        parentSearchNodeMetadata.setEstimatedTotalReward(new DoubleScalarReward(parentSearchNodeMetadata.getCumulativeReward().getValue() / parentSearchNodeMetadata.getVisitCounter()));

    }
}
