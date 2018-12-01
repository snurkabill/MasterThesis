package vahy.impl.model.reward;

import vahy.api.model.reward.RewardAggregator;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DoubleScalarRewardAggregator implements RewardAggregator<DoubleReward>  {

    @Override
    public DoubleReward emptyReward() {
        return new DoubleReward(0.0);
    }

    @Override
    public DoubleReward aggregate(DoubleReward first, DoubleReward second) {
        return new DoubleReward(first.getValue() + second.getValue());
    }

    @Override
    public DoubleReward aggregate(DoubleReward[] rewardArray) {
        double sum = 0.0;
        for (int i = 0; i < rewardArray.length; i++) {
            sum += rewardArray[i].getValue();
        }
        return new DoubleReward(sum);
    }

    @Override
    public DoubleReward aggregate(List<DoubleReward> doubleRewards) {
        double sum = 0.0;
        for (DoubleReward entry : doubleRewards) {
            sum += entry.getValue();
        }
        return new DoubleReward(sum);
    }

    @Override
    public DoubleReward aggregate(Stream<DoubleReward> rewards) {
        return rewards.reduce(this::aggregate).orElse(emptyReward());
    }

    @Override
    public DoubleReward aggregateDiscount(DoubleReward first, DoubleReward second, double discountFactor) {
        return new DoubleReward(first.getValue() + discountFactor * second.getValue());
    }

    @Override
    public DoubleReward aggregateDiscount(DoubleReward[] rewardArray, double discountFactor) {
        double discountedSum = 0.0;
        for (int i = 0; i < rewardArray.length; i++) {
            discountedSum += Math.pow(discountFactor, i) * rewardArray[i].getValue();
        }
        return new DoubleReward(discountedSum);
    }

    @Override
    public DoubleReward aggregateDiscount(List<DoubleReward> doubleRewards, double discountFactor) {
        double sum = 0.0;
        int iteration = 0;
        for (DoubleReward entry : doubleRewards) {
            sum += Math.pow(discountFactor, iteration) * entry.getValue();
            iteration++;
        }
        return new DoubleReward(sum);
    }

    @Override
    public DoubleReward aggregateDiscount(Stream<DoubleReward> rewards, double discountFactor) {
        return aggregateDiscount(rewards.collect(Collectors.toList()), discountFactor);
    }

    @Override
    public DoubleReward averageReward(DoubleReward[] rewardArray) {
        double sum = 0.0;
        for (int i = 0; i < rewardArray.length; i++) {
            sum += rewardArray[i].getValue();
        }
        return new DoubleReward(sum / rewardArray.length);
    }

    @Override
    public DoubleReward averageReward(List<DoubleReward> doubleRewards) {
        double sum = 0.0;
        for (DoubleReward entry : doubleRewards) {
            sum += entry.getValue();
        }
        return new DoubleReward(sum / doubleRewards.size());
    }

    @Override
    public DoubleReward averageReward(Stream<DoubleReward> doubleScalarRewardStream) {
        return doubleScalarRewardStream.reduce(new BinaryOperator<DoubleReward>() {

            private int count = 1;

            @Override
            public DoubleReward apply(DoubleReward doubleReward, DoubleReward doubleReward2) {
                double totalSum = doubleReward.getValue() * count;
                totalSum += doubleReward2.getValue();
                count++;
                return new DoubleReward(totalSum / count);
            }
        }).orElseThrow(() -> new IllegalStateException("Cannot compute average reward from empty stream"));
    }

    @Override
    public DoubleReward averageReward(DoubleReward runningAverage, int countOfAlreadyAveragedRewards, DoubleReward newReward) {
        return new DoubleReward((runningAverage.getValue() * countOfAlreadyAveragedRewards + newReward.getValue()) / (countOfAlreadyAveragedRewards + 1));
    }
}
