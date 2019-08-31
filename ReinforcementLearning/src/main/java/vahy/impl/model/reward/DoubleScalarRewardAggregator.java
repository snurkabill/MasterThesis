package vahy.impl.model.reward;

import vahy.api.model.reward.RewardAggregator;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DoubleScalarRewardAggregator implements RewardAggregator  {

    @Override
    public double emptyReward() {
        return 0.0;
    }

    @Override
    public double negate(double reward) {
        return -reward;
    }

    @Override
    public double aggregate(double first, double second) {
        return first + second;
    }

    @Override
    public double aggregate(double[] rewardArray) {
        double sum = 0.0;
        for (int i = 0; i < rewardArray.length; i++) {
            sum += rewardArray[i];
        }
        return sum;
    }

    @Override
    public double aggregate(List<Double> doubleRewards) {
        double sum = 0.0;
        for (double entry : doubleRewards) {
            sum += entry;
        }
        return sum;
    }

    @Override
    public double aggregate(Stream<Double> rewards) {
        return rewards.reduce(this::aggregate).orElse(emptyReward());
    }

    @Override
    public double aggregateDiscount(double first, double second, double discountFactor) {
        return first + discountFactor * second;
    }

    @Override
    public double aggregateDiscount(double[] rewardArray, double discountFactor) {
        double discountedSum = 0.0;
        for (int i = 0; i < rewardArray.length; i++) {
            discountedSum += Math.pow(discountFactor, i) * rewardArray[i];
        }
        return discountedSum;
    }

    @Override
    public double aggregateDiscount(List<Double> doubleRewards, double discountFactor) {
        double sum = 0.0;
        int iteration = 0;
        for (double entry : doubleRewards) {
            sum += Math.pow(discountFactor, iteration) * entry;
            iteration++;
        }
        return sum;
    }

    @Override
    public double aggregateDiscount(Stream<Double> rewards, double discountFactor) {
        return aggregateDiscount(rewards.collect(Collectors.toList()), discountFactor);
    }

    @Override
    public double averageReward(double[] rewardArray) {
        double sum = 0.0;
        for (int i = 0; i < rewardArray.length; i++) {
            sum += rewardArray[i];
        }
        return sum / rewardArray.length;
    }

    @Override
    public double averageReward(List<Double> doubleRewards) {
        double sum = 0.0;
        for (double entry : doubleRewards) {
            sum += entry;
        }
        return sum / doubleRewards.size();
    }

    @Override
    public double averageReward(Stream<Double> doubleScalarRewardStream) {
        return doubleScalarRewardStream.reduce(new BinaryOperator<>() {

            private int count = 1;

            @Override
            public Double apply(Double doubleReward, Double doubleReward2) {
                double totalSum = doubleReward * count;
                totalSum += doubleReward2;
                count++;
                return totalSum / count;
            }
        }).orElseThrow(() -> new IllegalStateException("Cannot compute average reward from empty stream"));
    }

    @Override
    public double averageReward(double runningAverage, int countOfAlreadyAveragedRewards, double newReward) {
        return (runningAverage * countOfAlreadyAveragedRewards + newReward) / (countOfAlreadyAveragedRewards + 1);
    }
}
