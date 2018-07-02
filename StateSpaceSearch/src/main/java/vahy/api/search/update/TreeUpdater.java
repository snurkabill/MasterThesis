package vahy.api.search.update;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;

public interface TreeUpdater<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>> {

    void updateTree(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> expandedNode);
}
