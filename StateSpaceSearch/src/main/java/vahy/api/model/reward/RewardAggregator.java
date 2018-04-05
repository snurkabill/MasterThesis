package vahy.api.model.reward;

import java.util.List;

public interface RewardAggregator<TReward extends Reward> {

    TReward aggregate(TReward first, TReward second);

    TReward aggregate(TReward[] rewardArray);

    TReward aggregate(List<TReward> rewardList);

    TReward expectedReward(TReward[] rewardArray);

    TReward expectedReward(List<TReward> rewardList);

}
