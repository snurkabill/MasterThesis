package vahy.api.search;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;

public interface SearchTree<TState extends State<TAction, TReward, TObservation>, TAction extends Action, TReward extends Reward, TObservation extends Observation> {

    SearchNode<TAction, TReward, TObservation, TState> getRoot();

    SearchNode<TAction, TReward, TObservation, TState> selectNode(); // TODO: Add selecting policy

    SearchNode<TAction, TReward, TObservation, TState> expandNode(SearchNode<TAction, TReward, TObservation, TState> searchNode); // TODO: Add expanding policy

    void backpropagate(); // IDK yet

}
