package vahy.impl.search.node.nodeMetadata.empty;

import vahy.api.model.Action;
import vahy.api.model.reward.Reward;
import vahy.impl.search.node.nodeMetadata.AbstractSearchNodeMetadata;

import java.util.Map;

public class EmptySearchNodeMetadata<TAction extends Action, TReward extends Reward> extends AbstractSearchNodeMetadata<TAction, TReward, EmptyStateActionMetadata<TReward>> {

    public EmptySearchNodeMetadata(TReward cumulativeReward, Map<TAction, EmptyStateActionMetadata<TReward>> stateActionMetadataMap) {
        super(cumulativeReward, stateActionMetadataMap);
    }
}
