package vahy.impl.model.reward;

import vahy.api.model.reward.RewardFactory;

public class DoubleScalarRewardFactory implements RewardFactory {

    @Override
    public Double fromNumericVector(double[] vector) {
        if(vector.length != 1) {
            throw new IllegalArgumentException("Expected vactor of length 1. Got instead: [" + vector.length + "]");
        }
        return vector[0];
    }
}
