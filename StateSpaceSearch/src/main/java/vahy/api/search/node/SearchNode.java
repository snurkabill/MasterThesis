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
        TState extends State<TAction, ? extends Reward, TObservation>,
        TSearchNodeMetadata extends SearchNodeMetadata> {

    SearchNode<TAction, TReward, TObservation, TState, TSearchNodeMetadata> getParent();

    Map<TAction, SearchNode<TAction, TReward, TObservation, TState, TSearchNodeMetadata>> getChildNodeMap();

    void updateChildMap(TAction action, SearchNode<TAction, TReward, TObservation, TState, TSearchNodeMetadata> child);

    StateRewardReturn<TReward, State<TAction, TReward, TObservation>> applyAction(TAction action);

    TSearchNodeMetadata getSearchNodeMetadata();

    TState getWrappedState();

    boolean isFinalNode();

    boolean isRoot();

    boolean isLeaf();

    // TODO: add getting (avg?) reward
}
