package vahy.api.search.update;

import vahy.api.model.Action;
import vahy.api.model.Reward;
import vahy.api.search.node.SearchNodeMetadata;

public interface NodeTransitionUpdater<TAction extends Action, TReward extends Reward, TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward>> {

    void applyUpdate(TSearchNodeMetadata parent, TSearchNodeMetadata child);
}
