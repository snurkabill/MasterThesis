package vahy.api.model.reward;

import java.util.List;
import java.util.stream.Stream;

public interface RewardAggregator<TReward extends Reward> {

    TReward emptyReward();

    TReward aggregate(TReward first, TReward second);

    TReward aggregate(TReward[] rewardArray);

    TReward aggregate(List<TReward> rewardList);

    TReward aggregate(Stream<TReward> rewardList);

    TReward aggregateDiscount(TReward first, TReward second, double discountFactor);

    TReward aggregateDiscount(TReward[] rewardArray, double discountFactor);

    TReward aggregateDiscount(List<TReward> rewardList, double discountFactor);

    TReward aggregateDiscount(Stream<TReward> rewardList, double discountFactor);

    TReward averageReward(TReward[] rewardArray);

    TReward averageReward(List<TReward> rewardList);

    TReward averageReward(Stream<TReward> rewardStream);
}
