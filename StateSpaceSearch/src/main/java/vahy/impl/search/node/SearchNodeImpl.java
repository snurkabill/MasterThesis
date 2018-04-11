package vahy.impl.search.node;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.AbstractSearchNode;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;

import java.util.Map;

public class SearchNodeImpl<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>>
    extends AbstractSearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {

    private final Map<TAction, SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState>> childNodeMap;

    public SearchNodeImpl(
        TState wrappedState,
        TSearchNodeMetadata searchNodeMetadata,
        Map<TAction, SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState>> childNodeMap) {
        this(wrappedState, searchNodeMetadata, childNodeMap, null, null);
    }

    public SearchNodeImpl(
        TState wrappedState,
        TSearchNodeMetadata searchNodeMetadata,
        Map<TAction, SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState>> childNodeMap,
        SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> parent,
        TAction appliedAction) {
        super(wrappedState, parent, appliedAction, searchNodeMetadata);
        this.childNodeMap = childNodeMap;
    }

    @Override
    public Map<TAction, SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState>> getChildNodeMap() {
        return childNodeMap;
    }

    @Override
    public void updateChildMap(TAction action, SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> child) {
        childNodeMap.put(action, child);
    }

    @Override
    public StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> applyAction(TAction action) {
        return getWrappedState().applyAction(action);
    }

    @Override
    public boolean isLeaf() {
        return isFinalNode() || childNodeMap.isEmpty();
    }
}
