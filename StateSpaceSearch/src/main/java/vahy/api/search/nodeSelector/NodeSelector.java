package vahy.api.search.nodeSelector;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;

import java.util.Collection;

public interface NodeSelector<
        TAction extends Action,
        TReward extends Reward,
        TObservation extends Observation,
        TSearchNodeMetadata extends SearchNodeMetadata,
        TState extends State<TAction, TReward, TObservation>> {

    void addNode(SearchNode<TAction, TReward, TObservation, TState, TSearchNodeMetadata> node);

    void addNodes(Collection<SearchNode<TAction, TReward, TObservation, TState, TSearchNodeMetadata>> rootNodes);

    SearchNode<TAction, TReward, TObservation, TState, TSearchNodeMetadata> selectNextNode();

}
