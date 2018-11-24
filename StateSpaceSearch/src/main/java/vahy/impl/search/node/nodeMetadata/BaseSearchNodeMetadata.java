package vahy.impl.search.node.nodeMetadata;

import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNodeMetadata;

public class BaseSearchNodeMetadata<TReward extends Reward> implements SearchNodeMetadata<TReward> {

    private final TReward cumulativeReward;
    private final TReward gainedReward;
    private final TReward defaultEstimatedReward;
    private TReward expectedReward;

    public BaseSearchNodeMetadata(TReward cumulativeReward, TReward gainedReward, TReward defaultEstimatedReward) {
        this.cumulativeReward = cumulativeReward;
        this.gainedReward = gainedReward;
        this.defaultEstimatedReward = defaultEstimatedReward;
        this.expectedReward = defaultEstimatedReward;
    }

    @Override
    public TReward getDefaultEstimatedReward() {
        return defaultEstimatedReward;
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
    public void setExpectedReward(TReward expectedReward) {
        this.expectedReward = expectedReward;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\\nCumulativeRew: ");
        stringBuilder.append(this.cumulativeReward.toPrettyString());
        stringBuilder.append("\\nEstimatedRew: ");
        stringBuilder.append(this.expectedReward.toPrettyString());
        stringBuilder.append("\\nGainedReward: ");
        stringBuilder.append(this.gainedReward.toPrettyString());
        stringBuilder.append("\\nDefaultEstimatedReward: ");
        stringBuilder.append(this.defaultEstimatedReward.toPrettyString());
        return stringBuilder.toString();
    }
}
