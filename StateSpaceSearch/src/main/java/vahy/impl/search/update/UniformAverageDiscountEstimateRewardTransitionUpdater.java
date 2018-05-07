package vahy.impl.search.update;

import vahy.api.model.Action;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.impl.model.reward.DoubleScalarReward;

import java.util.Objects;

public class UniformAverageDiscountEstimateRewardTransitionUpdater<
        TAction extends Action,
        TStateActionMetadata extends StateActionMetadata<DoubleScalarReward>,
        TSearchNodeMetadata extends SearchNodeMetadata<TAction, DoubleScalarReward, TStateActionMetadata>>
    implements NodeTransitionUpdater<TAction, DoubleScalarReward, TStateActionMetadata, TSearchNodeMetadata> {

        private final double discountFactor;

    public UniformAverageDiscountEstimateRewardTransitionUpdater(double discountFactor) {
            this.discountFactor = discountFactor;
        }

        @Override
        public void applyUpdate(TSearchNodeMetadata parent, TSearchNodeMetadata child, TAction action) {
            parent.getStateActionMetadataMap().get(action).setEstimatedTotalReward(new DoubleScalarReward(discountFactor * child.getEstimatedTotalReward().getValue()));
            parent.setEstimatedTotalReward(new DoubleScalarReward(parent
                .getStateActionMetadataMap()
                .values()
                .stream()
                .map(StateActionMetadata::getEstimatedTotalReward)
                .filter(Objects::nonNull)
                .mapToDouble(DoubleScalarReward::getValue)
                .average()
                .getAsDouble()
                )
            );
        }
}
