package vahy.impl.search.nodeSelector;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;

public class BfsNodeSelector<
        TAction extends Action,
        TReward extends Reward,
        TObservation extends Observation,
        TSearchNodeMetadata extends SearchNodeMetadata,
        TState extends State<TAction, TReward, TObservation>>
        extends AbstractExhaustiveNodeSelector<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    @Override
    public void addNode(SearchNode<TAction, TReward, TObservation, TState, TSearchNodeMetadata> node) {
        nodeQueue.addLast(node);
    }
}
