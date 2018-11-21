package vahy.api.search.update;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;

public interface NodeTransitionUpdater<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata<TReward>,
    TState extends State<TAction, TReward, TObservation, TState>> {

    void applyUpdate(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> evaluatedNode,
                     SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> parent,
                     SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> child);
}
