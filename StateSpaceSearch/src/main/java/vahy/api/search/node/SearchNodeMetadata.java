package vahy.api.search.node;

import vahy.api.model.reward.Reward;

public interface SearchNodeMetadata<TReward extends Reward> {

    TReward getCumulativeReward();

    TReward getGainedReward();

    TReward getDefaultEstimatedReward();

    TReward getExpectedReward();

    void setExpectedReward(TReward expectedReward);

}
