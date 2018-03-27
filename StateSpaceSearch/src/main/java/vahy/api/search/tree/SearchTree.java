package vahy.api.search.tree;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;

public interface SearchTree<
        TAction extends Action,
        TReward extends Reward,
        TObservation extends Observation,
        TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward>,
        TState extends State<TAction, TReward, TObservation>> {

    SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> getRoot();

    void updateTree();

}
