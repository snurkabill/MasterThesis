package vahy.impl.model.reward;

import vahy.api.model.reward.RewardAggregator;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DoubleScalarRewardAggregator implements RewardAggregator<DoubleScalarReward>  {

    @Override
    public DoubleScalarReward emptyReward() {
        return new DoubleScalarReward(0.0);
    }

    @Override
    public DoubleScalarReward aggregate(DoubleScalarReward first, DoubleScalarReward second) {
        return new DoubleScalarReward(first.getValue() + second.getValue());
    }

    @Override
    public DoubleScalarReward aggregate(DoubleScalarReward[] rewardArray) {
        double sum = 0.0;
        for (int i = 0; i < rewardArray.length; i++) {
            sum += rewardArray[i].getValue();
        }
        return new DoubleScalarReward(sum);
    }

    @Override
    public DoubleScalarReward aggregate(List<DoubleScalarReward> doubleScalarRewards) {
        double sum = 0.0;
        for (DoubleScalarReward entry : doubleScalarRewards) {
            sum += entry.getValue();
        }
        return new DoubleScalarReward(sum);
    }

    @Override
    public DoubleScalarReward aggregate(Stream<DoubleScalarReward> rewards) {
        return rewards.reduce(this::aggregate).orElse(emptyReward());
    }

    @Override
    public DoubleScalarReward aggregateDiscount(DoubleScalarReward first, DoubleScalarReward second, double discountFactor) {
        return new DoubleScalarReward(first.getValue() + discountFactor * second.getValue());
    }

    @Override
    public DoubleScalarReward aggregateDiscount(DoubleScalarReward[] rewardArray, double discountFactor) {
        double discountedSum = 0.0;
        for (int i = 0; i < rewardArray.length; i++) {
            discountedSum += Math.pow(discountFactor, i) * rewardArray[i].getValue();
        }
        return new DoubleScalarReward(discountedSum);
    }

    @Override
    public DoubleScalarReward aggregateDiscount(List<DoubleScalarReward> doubleScalarRewards, double discountFactor) {
        double sum = 0.0;
        int iteration = 0;
        for (DoubleScalarReward entry : doubleScalarRewards) {
            sum += Math.pow(discountFactor, iteration) * entry.getValue();
            iteration++;
        }
        return new DoubleScalarReward(sum);
    }

    @Override
    public DoubleScalarReward aggregateDiscount(Stream<DoubleScalarReward> rewards, double discountFactor) {
        return aggregateDiscount(rewards.collect(Collectors.toList()), discountFactor);
    }

    @Override
    public DoubleScalarReward averageReward(DoubleScalarReward[] rewardArray) {
        double sum = 0.0;
        for (int i = 0; i < rewardArray.length; i++) {
            sum += rewardArray[i].getValue();
        }
        return new DoubleScalarReward(sum / rewardArray.length);
    }

    @Override
    public DoubleScalarReward averageReward(List<DoubleScalarReward> doubleScalarRewards) {
        double sum = 0.0;
        for (DoubleScalarReward entry : doubleScalarRewards) {
            sum += entry.getValue();
        }
        return new DoubleScalarReward(sum / doubleScalarRewards.size());
    }

    @Override
    public DoubleScalarReward averageReward(Stream<DoubleScalarReward> doubleScalarRewardStream) {
        return doubleScalarRewardStream.reduce(new BinaryOperator<DoubleScalarReward>() {

            private int count = 1;

            @Override
            public DoubleScalarReward apply(DoubleScalarReward doubleScalarReward, DoubleScalarReward doubleScalarReward2) {
                double totalSum = doubleScalarReward.getValue() * count;
                totalSum += doubleScalarReward2.getValue();
                count++;
                return new DoubleScalarReward(totalSum / count);
            }
        }).orElseThrow(() -> new IllegalStateException("Cannot compute average reward from empty stream"));
    }

}
