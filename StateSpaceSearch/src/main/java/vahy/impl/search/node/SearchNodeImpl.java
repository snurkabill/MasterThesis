package vahy.impl.search.node;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.AbstractSearchNode;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;

import java.util.Map;

public class SearchNodeImpl<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata<TReward>,
    TState extends State<TAction, TReward, TObservation, TState>>
    extends AbstractSearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    private final Map<TAction, SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> childNodeMap;

    public SearchNodeImpl(
        TState wrappedState,
        TSearchNodeMetadata searchNodeMetadata,
        Map<TAction, SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> childNodeMap) {
        this(wrappedState, searchNodeMetadata, childNodeMap, null, null);
    }

    public SearchNodeImpl(
        TState wrappedState,
        TSearchNodeMetadata searchNodeMetadata,
        Map<TAction, SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> childNodeMap,
        SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> parent,
        TAction appliedAction) {
        super(wrappedState, parent, appliedAction, searchNodeMetadata);
        this.childNodeMap = childNodeMap;
    }

    @Override
    public Map<TAction, SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> getChildNodeMap() {
        return childNodeMap;
    }

    @Override
    public StateRewardReturn<TAction, TReward, TObservation, TState> applyAction(TAction action) {
        return getWrappedState().applyAction(action);
    }

    @Override
    public boolean isLeaf() {
        return isFinalNode() || childNodeMap.isEmpty();
    }
}
