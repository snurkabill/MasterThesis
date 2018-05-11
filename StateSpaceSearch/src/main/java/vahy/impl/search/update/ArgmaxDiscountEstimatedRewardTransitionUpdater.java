package vahy.impl.search.update;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.api.search.update.NodeTransitionUpdater;

public class ArgmaxDiscountEstimatedRewardTransitionUpdater<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>>
    implements NodeTransitionUpdater<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {

    private final double discountFactor;
    private final RewardAggregator<TReward> rewardAggregator;

    public ArgmaxDiscountEstimatedRewardTransitionUpdater(double discountFactor, RewardAggregator<TReward> rewardAggregator) {
        this.discountFactor = discountFactor;
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public void applyUpdate(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> parent,
                            SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> child,
                            TAction action) {
        TSearchNodeMetadata parentSearchNodeMetadata = parent.getSearchNodeMetadata();
        parentSearchNodeMetadata.getStateActionMetadataMap().get(action).setEstimatedTotalReward(
            rewardAggregator.aggregateDiscount(parentSearchNodeMetadata.getStateActionMetadataMap().get(action).getGainedReward(),
                child.getSearchNodeMetadata().getEstimatedTotalReward(),
                discountFactor)
        );
        parentSearchNodeMetadata.setEstimatedTotalReward(
            parentSearchNodeMetadata
                .getStateActionMetadataMap()
                .values()
                .stream()
                .map(StateActionMetadata::getEstimatedTotalReward)
                .max(Comparable::compareTo)
                .orElseThrow(() -> new IllegalStateException("Children should be always expanded when doing transition update")));
    }
}
