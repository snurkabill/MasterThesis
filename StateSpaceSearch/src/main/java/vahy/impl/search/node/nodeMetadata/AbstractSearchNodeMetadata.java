package vahy.impl.search.node.nodeMetadata;

import vahy.api.model.Action;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;

import java.util.Map;

public abstract class AbstractSearchNodeMetadata<TAction extends Action, TReward extends Reward, TStateActionMetadata extends StateActionMetadata<TReward>> implements SearchNodeMetadata<TAction, TReward, TStateActionMetadata> {

    private final Map<TAction, TStateActionMetadata> stateActionMetadataMap;
    private final TReward cumulativeReward;
    private TReward estimatedTotalReward;

    public AbstractSearchNodeMetadata(TReward cumulativeReward, Map<TAction, TStateActionMetadata> stateActionMetadataMap) {
        this.cumulativeReward = cumulativeReward;
        this.stateActionMetadataMap = stateActionMetadataMap;
    }

    @Override
    public Map<TAction, TStateActionMetadata> getStateActionMetadataMap() {
        return stateActionMetadataMap;
    }

    @Override
    public TReward getCumulativeReward() {
        return cumulativeReward;
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
