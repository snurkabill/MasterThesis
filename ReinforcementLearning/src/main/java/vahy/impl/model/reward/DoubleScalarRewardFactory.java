package vahy.impl.model.reward;

import vahy.api.model.reward.RewardFactory;

public class DoubleScalarRewardFactory implements RewardFactory<DoubleScalarRewardDouble> {

    @Override
    public DoubleScalarRewardDouble fromNumericVector(double[] vector) {
        if(vector.length != 1) {
            throw new IllegalArgumentException("Expected vactor of length 1. Got instead: [" + vector.length + "]");
        }
        return new DoubleScalarRewardDouble(vector[0]);
    }
}
