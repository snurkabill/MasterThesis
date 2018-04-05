package vahy.impl.search.node.nodeMetadata.empty;

import vahy.api.model.reward.Reward;
import vahy.impl.search.node.nodeMetadata.AbstractStateActionMetadata;

public class EmptyStateActionMetadata<TReward extends Reward> extends AbstractStateActionMetadata<TReward> {

    public EmptyStateActionMetadata(TReward gainedReward) {
        super(gainedReward);
    }
}
