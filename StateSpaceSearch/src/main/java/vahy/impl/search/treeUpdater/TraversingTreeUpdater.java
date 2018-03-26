package vahy.impl.search.treeUpdater;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.treeUpdater.TreeUpdater;

public class TraversingTreeUpdater<
        TAction extends Action,
        TReward extends Reward,
        TObservation extends Observation,
        TSearchNodeMetadata extends SearchNodeMetadata,
        TState extends State<TAction, TReward, TObservation>>
        implements TreeUpdater<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    @Override
    public void updateTree(SearchNode<TAction, TReward, TObservation, TState, TSearchNodeMetadata> expandedNode) {

        // TODO !!!

    }
}
