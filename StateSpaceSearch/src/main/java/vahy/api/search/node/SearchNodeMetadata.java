package vahy.api.search.node;

import vahy.api.model.reward.Reward;

public interface SearchNodeMetadata<TReward extends Reward> {

    TReward getCumulativeReward();

    TReward getGainedReward();

    TReward getPredictedReward();

    TReward getExpectedReward();

    void setPredictedReward(TReward predictedReward);

    void setExpectedReward(TReward expectedReward);

}
