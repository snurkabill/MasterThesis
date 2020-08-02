package vahy.impl.model.reward;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DoubleScalarRewardAggregator {

    private DoubleScalarRewardAggregator() {
    }

    public static double emptyReward() {
        return 0.0;
    }

    public static double negate(double reward) {
        return -reward;
    }

    public static double aggregate(double first, double second) {
        return first + second;
    }

    public static double aggregate(double[] rewardArray) {
        double sum = 0.0;
        for (int i = 0; i < rewardArray.length; i++) {
            sum += rewardArray[i];
        }
        return sum;
    }

    public static double aggregate(List<Double> doubleRewards) {
        double sum = 0.0;
        for (double entry : doubleRewards) {
            sum += entry;
        }
        return sum;
    }

    public static double aggregate(Stream<Double> rewards) {
        return rewards.reduce(DoubleScalarRewardAggregator::aggregate).orElse(emptyReward());
    }

    public static double aggregateDiscount(double first, double second, double discountFactor) {
        return first + discountFactor * second;
    }

    public static double aggregateDiscount(double[] rewardArray, double discountFactor) {
        double discountedSum = 0.0;
        for (int i = 0; i < rewardArray.length; i++) {
            discountedSum += Math.pow(discountFactor, i) * rewardArray[i];
        }
        return discountedSum;
    }

    public static double aggregateDiscount(List<Double> doubleRewards, double discountFactor) {
        double sum = 0.0;
        int iteration = 0;
        for (double entry : doubleRewards) {
            sum += Math.pow(discountFactor, iteration) * entry;
            iteration++;
        }
        return sum;
    }

    public static double aggregateDiscount(Stream<Double> rewards, double discountFactor) {
        return aggregateDiscount(rewards.collect(Collectors.toList()), discountFactor);
    }

    public static double averageReward(double[] rewardArray) {
        double sum = 0.0;
        for (int i = 0; i < rewardArray.length; i++) {
            sum += rewardArray[i];
        }
        return sum / rewardArray.length;
    }

    public static double averageReward(List<Double> doubleRewards) {
        double sum = 0.0;
        for (double entry : doubleRewards) {
            sum += entry;
        }
        return sum / doubleRewards.size();
    }

    public static double averageReward(Stream<Double> doubleScalarRewardStream) {
        return doubleScalarRewardStream
            .mapToDouble(x -> x)
            .average()
            .orElseThrow(() -> new IllegalStateException("Cannot compute average reward from empty stream"));
    }

    
    public static double averageReward(double runningAverage, int countOfAlreadyAveragedRewards, double newReward) {
        return (runningAverage * countOfAlreadyAveragedRewards + newReward) / (countOfAlreadyAveragedRewards + 1);
    }
}
