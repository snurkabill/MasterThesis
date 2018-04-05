package vahy.impl.search.node.nodeMetadata;

import vahy.api.model.reward.Reward;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;

public class AbstractStateActionMetadata<TReward extends Reward> implements StateActionMetadata<TReward> {

    private final TReward gainedReward;

    public AbstractStateActionMetadata(TReward gainedReward) {
        this.gainedReward = gainedReward;
    }

    @Override
    public TReward getGainedReward() {
        return gainedReward;
    }
}
