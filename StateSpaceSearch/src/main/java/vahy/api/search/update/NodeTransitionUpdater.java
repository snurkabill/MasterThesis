package vahy.api.search.update;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;

public interface NodeTransitionUpdater<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>> {

    void applyUpdate(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> parent,
                     SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> child,
                     TAction action);
}
