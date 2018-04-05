package vahy.api.search.node.nodeMetadata;

import vahy.api.model.reward.Reward;

public interface StateActionMetadata<TReward extends Reward> {

    TReward getGainedReward();

}
