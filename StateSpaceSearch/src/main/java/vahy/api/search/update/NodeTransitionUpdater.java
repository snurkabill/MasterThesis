package vahy.api.search.update;

import vahy.api.model.Action;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;

public interface NodeTransitionUpdater<
    TAction extends Action,
    TReward extends Reward,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>> {

    void applyUpdate(TSearchNodeMetadata parent, TSearchNodeMetadata child, TAction action);
}
