package vahy.api.model.reward;

import java.util.List;
import java.util.stream.Stream;

public interface RewardAggregator {

    double emptyReward();

    double negate(double reward);

    double aggregate(double first, double second);

    double aggregate(double[] rewardArray);

    double aggregate(List<Double> rewardList);

    double aggregate(Stream<Double> rewardList);

    double aggregateDiscount(double first, double second, double discountFactor);

    double aggregateDiscount(double[] rewardArray, double discountFactor);

    double aggregateDiscount(List<Double> rewardList, double discountFactor);

    double aggregateDiscount(Stream<Double> rewardList, double discountFactor);

    double averageReward(double[] rewardArray);

    double averageReward(List<Double> rewardList);

    double averageReward(Stream<Double> rewardStream);

    double averageReward(double runningAverage, int countOfAlreadyAveragedRewards, double newReward);
}
