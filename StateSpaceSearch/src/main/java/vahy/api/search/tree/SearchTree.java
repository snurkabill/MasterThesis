package vahy.api.search.tree;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;

public interface SearchTree<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> getRoot();

    StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState> applyAction(TAction action);

    boolean updateTree();

}
