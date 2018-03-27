package vahy.api.search.node;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;

import java.util.Map;

public interface SearchNode<
        TAction extends Action,
        TReward extends Reward,
        TObservation extends Observation,
        TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward>,
        TState extends State<TAction, TReward, TObservation>> {

    SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> getParent();

    Map<TAction, SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> getChildNodeMap();

    void updateChildMap(TAction action, SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> child);

    StateRewardReturn<TReward, State<TAction, TReward, TObservation>> applyAction(TAction action);

    TAction[] getAllPossibleActions();

    TSearchNodeMetadata getSearchNodeMetadata();

    TState getWrappedState();

    boolean isFinalNode();

    boolean isRoot();

    boolean isLeaf();

    // TODO: add getting (avg?) reward
}
