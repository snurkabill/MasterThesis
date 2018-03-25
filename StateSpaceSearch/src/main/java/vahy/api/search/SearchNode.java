package vahy.api.search;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;

import java.util.Map;

public interface SearchNode<TAction extends Action, TReward extends Reward, TObservation extends Observation, TState extends State<TAction, ? extends Reward, TObservation>> {

    SearchNode<TAction, TReward, TObservation, TState> getParent();

    Map<TAction, SearchNode<TAction, TReward, TObservation, TState>> getChildNodeMap();

    SearchNode<TAction, TReward, TObservation, TState> applyAction(TAction action);

    SearchNodeMetadata getSearchNodeMetadata();

    TState getWrappedState();

    boolean isFinalNode();
}
