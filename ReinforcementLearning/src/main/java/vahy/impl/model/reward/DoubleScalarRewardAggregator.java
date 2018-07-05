package vahy.impl.model.reward;

import vahy.api.model.reward.RewardAggregator;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DoubleScalarRewardAggregator implements RewardAggregator<DoubleScalarRewardDouble>  {

    @Override
    public DoubleScalarRewardDouble emptyReward() {
        return new DoubleScalarRewardDouble(0.0);
    }

    @Override
    public DoubleScalarRewardDouble aggregate(DoubleScalarRewardDouble first, DoubleScalarRewardDouble second) {
        return new DoubleScalarRewardDouble(first.getValue() + second.getValue());
    }

    @Override
    public DoubleScalarRewardDouble aggregate(DoubleScalarRewardDouble[] rewardArray) {
        double sum = 0.0;
        for (int i = 0; i < rewardArray.length; i++) {
            sum += rewardArray[i].getValue();
        }
        return new DoubleScalarRewardDouble(sum);
    }

    @Override
    public DoubleScalarRewardDouble aggregate(List<DoubleScalarRewardDouble> doubleScalarRewards) {
        double sum = 0.0;
        for (DoubleScalarRewardDouble entry : doubleScalarRewards) {
            sum += entry.getValue();
        }
        return new DoubleScalarRewardDouble(sum);
    }

    @Override
    public DoubleScalarRewardDouble aggregate(Stream<DoubleScalarRewardDouble> rewards) {
        return rewards.reduce(this::aggregate).orElse(emptyReward());
    }

    @Override
    public DoubleScalarRewardDouble aggregateDiscount(DoubleScalarRewardDouble first, DoubleScalarRewardDouble second, double discountFactor) {
        return new DoubleScalarRewardDouble(first.getValue() + discountFactor * second.getValue());
    }

    @Override
    public DoubleScalarRewardDouble aggregateDiscount(DoubleScalarRewardDouble[] rewardArray, double discountFactor) {
        double discountedSum = 0.0;
        for (int i = 0; i < rewardArray.length; i++) {
            discountedSum += Math.pow(discountFactor, i) * rewardArray[i].getValue();
        }
        return new DoubleScalarRewardDouble(discountedSum);
    }

    @Override
    public DoubleScalarRewardDouble aggregateDiscount(List<DoubleScalarRewardDouble> doubleScalarRewards, double discountFactor) {
        double sum = 0.0;
        int iteration = 0;
        for (DoubleScalarRewardDouble entry : doubleScalarRewards) {
            sum += Math.pow(discountFactor, iteration) * entry.getValue();
            iteration++;
        }
        return new DoubleScalarRewardDouble(sum);
    }

    @Override
    public DoubleScalarRewardDouble aggregateDiscount(Stream<DoubleScalarRewardDouble> rewards, double discountFactor) {
        return aggregateDiscount(rewards.collect(Collectors.toList()), discountFactor);
    }

    @Override
    public DoubleScalarRewardDouble averageReward(DoubleScalarRewardDouble[] rewardArray) {
        double sum = 0.0;
        for (int i = 0; i < rewardArray.length; i++) {
            sum += rewardArray[i].getValue();
        }
        return new DoubleScalarRewardDouble(sum / rewardArray.length);
    }

    @Override
    public DoubleScalarRewardDouble averageReward(List<DoubleScalarRewardDouble> doubleScalarRewards) {
        double sum = 0.0;
        for (DoubleScalarRewardDouble entry : doubleScalarRewards) {
            sum += entry.getValue();
        }
        return new DoubleScalarRewardDouble(sum / doubleScalarRewards.size());
    }

    @Override
    public DoubleScalarRewardDouble averageReward(Stream<DoubleScalarRewardDouble> doubleScalarRewardStream) {
        return doubleScalarRewardStream.reduce(new BinaryOperator<DoubleScalarRewardDouble>() {

            private int count = 1;

            @Override
            public DoubleScalarRewardDouble apply(DoubleScalarRewardDouble doubleScalarReward, DoubleScalarRewardDouble doubleScalarReward2) {
                double totalSum = doubleScalarReward.getValue() * count;
                totalSum += doubleScalarReward2.getValue();
                count++;
                return new DoubleScalarRewardDouble(totalSum / count);
            }
        }).orElseThrow(() -> new IllegalStateException("Cannot compute average reward from empty stream"));
    }

    @Override
    public DoubleScalarRewardDouble averageReward(DoubleScalarRewardDouble runningAverage, int countOfAlreadyAveragedRewards, DoubleScalarRewardDouble newReward) {
        return new DoubleScalarRewardDouble((runningAverage.getValue() * countOfAlreadyAveragedRewards + newReward.getValue()) / (countOfAlreadyAveragedRewards + 1));
    }
}
