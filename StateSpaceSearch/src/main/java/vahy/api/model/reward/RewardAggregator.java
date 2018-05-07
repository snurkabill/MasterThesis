package vahy.api.model.reward;

import java.util.List;
import java.util.stream.Stream;

public interface RewardAggregator<TReward extends Reward> {

    TReward aggregate(TReward first, TReward second);

    TReward aggregate(TReward[] rewardArray);

    TReward aggregate(List<TReward> rewardList);

    TReward aggregate(Stream<TReward> rewardList);

    TReward averageReward(TReward[] rewardArray);

    TReward averageReward(List<TReward> rewardList);

    TReward averageReward(Stream<TReward> rewardStream);
}
