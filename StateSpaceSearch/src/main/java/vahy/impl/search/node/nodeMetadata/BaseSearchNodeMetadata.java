package vahy.impl.search.node.nodeMetadata;

import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNodeMetadata;

public class BaseSearchNodeMetadata<TReward extends Reward> implements SearchNodeMetadata<TReward> {

    private final TReward cumulativeReward;
    private final TReward gainedReward;
    private TReward predictedReward;
    private TReward expectedReward;

    public BaseSearchNodeMetadata(TReward cumulativeReward, TReward gainedReward, TReward predictedReward) {
        this.cumulativeReward = cumulativeReward;
        this.gainedReward = gainedReward;
        this.predictedReward = predictedReward;
        this.expectedReward = predictedReward;
    }

    @Override
    public TReward getPredictedReward() {
        return predictedReward;
    }

    @Override
    public TReward getCumulativeReward() {
        return cumulativeReward;
    }

    @Override
    public TReward getGainedReward() {
        return this.gainedReward;
    }

    @Override
    public TReward getExpectedReward() {
        return expectedReward;
    }

    @Override
    public void setPredictedReward(TReward predictedReward) {
        this.predictedReward = predictedReward;
    }

    @Override
    public void setExpectedReward(TReward expectedReward) {
        this.expectedReward = expectedReward;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\\nCumulativeRew: ");
        stringBuilder.append(this.cumulativeReward.toPrettyString());
        stringBuilder.append("\\nExpectedRew: ");
        stringBuilder.append(this.expectedReward.toPrettyString());
        stringBuilder.append("\\nGainedReward: ");
        stringBuilder.append(this.gainedReward.toPrettyString());
        stringBuilder.append("\\nPredictedReward: ");
        stringBuilder.append(this.predictedReward.toPrettyString());
        return stringBuilder.toString();
    }
}
