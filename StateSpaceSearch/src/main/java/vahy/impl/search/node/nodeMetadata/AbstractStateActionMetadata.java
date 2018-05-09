package vahy.impl.search.node.nodeMetadata;

import vahy.api.model.reward.Reward;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;

public abstract class AbstractStateActionMetadata<TReward extends Reward> implements StateActionMetadata<TReward> {

    private final TReward gainedReward;
    private TReward estimatedTotalReward;

    public AbstractStateActionMetadata(TReward gainedReward) {
        this.gainedReward = gainedReward;
    }

    @Override
    public TReward getGainedReward() {
        return gainedReward;
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
