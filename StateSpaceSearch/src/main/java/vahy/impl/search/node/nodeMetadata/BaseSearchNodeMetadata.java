package vahy.impl.search.node.nodeMetadata;

import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNodeMetadata;

public class BaseSearchNodeMetadata<TReward extends Reward> implements SearchNodeMetadata<TReward> {

    private final TReward cumulativeReward;
    private final TReward gainedReward;
    private final TReward defaultEstimatedReward;
    private TReward estimatedTotalReward;

    public BaseSearchNodeMetadata(TReward cumulativeReward, TReward gainedReward, TReward defaultEstimatedReward) {
        this.cumulativeReward = cumulativeReward;
        this.gainedReward = gainedReward;
        this.defaultEstimatedReward = defaultEstimatedReward;
        this.estimatedTotalReward = defaultEstimatedReward;
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
    public TReward getEstimatedTotalReward() {
        return estimatedTotalReward;
    }

    @Override
    public void setEstimatedTotalReward(TReward estimatedTotalReward) {
        this.estimatedTotalReward = estimatedTotalReward;
    }
}
