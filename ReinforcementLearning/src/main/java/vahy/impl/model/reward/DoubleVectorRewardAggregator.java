package vahy.impl.model.reward;

import vahy.utils.ImmutableTuple;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class DoubleVectorRewardAggregator {

    public static double[] emptyReward(int size) {
        return new double[size];
    }

    public static double[] negate(double[] reward) {
        for (int i = 0; i < reward.length; i++) {
            reward[i] = -reward[i];
        }
        return reward;
    }

    public static double[] aggregate(double[] first, double[] second) {
        if(first.length != second.length) {
            throw new IllegalStateException("Different array lengths");
        }
        var copy = Arrays.copyOf(first, first.length);
        for (int i = 0; i < copy.length; i++) {
            copy[i] += second[i];
        }
        return copy;
    }


    public static void aggregateInFirstPlace(double[] first, double[] second) {
        if(first.length != second.length) {
            throw new IllegalStateException("Different array lengths");
        }
        for (int i = 0; i < first.length; i++) {
            first[i] += second[i];
        }
    }

//    public static double[] aggregate(double[][] rewardArray) {
//        double sum = 0.0;
//        for (int i = 0; i < rewardArray.length; i++) {
//            sum += rewardArray[i];
//        }
//        return sum;
//    }
//
//    public static double aggregate(List<Double> doubleRewards) {
//        double sum = 0.0;
//        for (double entry : doubleRewards) {
//            sum += entry;
//        }
//        return sum;
//    }
//
//    public static double aggregate(Stream<Double> rewards) {
//        return rewards.reduce(DoubleVectorRewardAggregator::aggregate).orElse(emptyReward());
//    }

    public static double[] aggregateDiscount(double[] first, double[] second, double discountFactor) {
        if(first.length != second.length) {
            throw new IllegalStateException("Different array lengths");
        }
        var array = Arrays.copyOf(first, first.length);
        for (int i = 0; i < array.length; i++) {
            array[i] += discountFactor * second[i];
        }
        return array;
    }

    public static void aggregateDiscountInSecondPlace(double[] first, double[] second, double discountFactor) {
        if(first.length != second.length) {
            throw new IllegalStateException("Different array lengths");
        }
        for (int i = 0; i < first.length; i++) {
            second[i] = first[i] + discountFactor * second[i];
        }
    }

//
//    public static double aggregateDiscount(double[] rewardArray, double discountFactor) {
//        double discountedSum = 0.0;
//        for (int i = 0; i < rewardArray.length; i++) {
//            discountedSum += Math.pow(discountFactor, i) * rewardArray[i];
//        }
//        return discountedSum;
//    }

    public static double[] aggregateDiscount(List<double[]> doubleRewards, double discountFactor) {
        var sum = new double[doubleRewards.get(0).length];
        int iteration = 0;
        for (double[] entry : doubleRewards) {
            var multiplier = Math.pow(discountFactor, iteration);
            for (int i = 0; i < entry.length; i++) {
                sum[i] += multiplier * entry[i];
            }
            iteration++;
        }
        return sum;
    }

//    public static double aggregateDiscount(Stream<Double> rewards, double discountFactor) {
//        return aggregateDiscount(rewards.collect(Collectors.toList()), discountFactor);
//    }

    public static double[] averageReward(double[][] rewardArray) {
        var sum = new double[rewardArray[0].length];
        for (double[] entry : rewardArray) {
            for (int i = 0; i < entry.length; i++) {
                sum[i] += entry[i];
            }
        }
        for (int i = 0; i < sum.length; i++) {
            sum[i] /= rewardArray.length;
        }
        return sum;
    }

    public static double[] averageReward(List<double[]> doubleRewards) {
        var sum = new double[doubleRewards.get(0).length];
        for (double[] entry : doubleRewards) {
            for (int i = 0; i < entry.length; i++) {
                sum[i] += entry[i];
            }
        }
        for (int i = 0; i < sum.length; i++) {
            sum[i] /= doubleRewards.size();
        }
        return sum;
    }

    public static double[] averageReward(Stream<double[]> doubleScalarRewardStream) {
        var reduction = doubleScalarRewardStream
            .map(x -> new ImmutableTuple<>(1, x))
            .reduce((x, y) -> {
                var doubles = x.getSecond();
                var doubles2 = y.getSecond();
                for (int i = 0; i < doubles.length; i++) {
                    doubles[i] += doubles2[i];
                }
                return new ImmutableTuple<>(x.getFirst() + y.getFirst(), doubles);
            }).orElseThrow();

        var doubles = reduction.getSecond();
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] /= reduction.getFirst();
        }
        return doubles;
    }

    
//    public static double averageReward(double runningAverage, int countOfAlreadyAveragedRewards, double newReward) {
//        return (runningAverage * countOfAlreadyAveragedRewards + newReward) / (countOfAlreadyAveragedRewards + 1);
//    }
}
