package vahy.api.search.nodeSelector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;

public interface NodeSelector<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata<TReward>,
    TState extends State<TAction, TReward, TObservation, TState>> {

    void setNewRoot(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> root);

    SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> selectNextNode();

}
