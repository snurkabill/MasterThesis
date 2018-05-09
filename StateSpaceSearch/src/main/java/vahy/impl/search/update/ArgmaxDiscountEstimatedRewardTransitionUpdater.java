package vahy.impl.search.update;

import vahy.api.model.Action;
import vahy.api.model.reward.Reward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.api.search.update.NodeTransitionUpdater;

public class ArgmaxDiscountEstimatedRewardTransitionUpdater<
    TAction extends Action,
    TReward extends Reward,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>>
    implements NodeTransitionUpdater<TAction, TReward, TStateActionMetadata, TSearchNodeMetadata> {

    private final double discountFactor;
    private final RewardAggregator<TReward> rewardAggregator;

    public ArgmaxDiscountEstimatedRewardTransitionUpdater(double discountFactor, RewardAggregator<TReward> rewardAggregator) {
        this.discountFactor = discountFactor;
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public void applyUpdate(TSearchNodeMetadata parent, TSearchNodeMetadata child, TAction action) {
        parent.getStateActionMetadataMap().get(action).setEstimatedTotalReward(
            rewardAggregator.aggregateDiscount(parent.getStateActionMetadataMap().get(action).getGainedReward(),
                child.getEstimatedTotalReward(),
                discountFactor)
        );
        parent.setEstimatedTotalReward(
            parent
                .getStateActionMetadataMap()
                .values()
                .stream()
                .map(StateActionMetadata::getEstimatedTotalReward)
                .max(Comparable::compareTo)
                .orElseThrow(() -> new IllegalStateException("Children should be always expanded when doing transition update")));
    }
}
