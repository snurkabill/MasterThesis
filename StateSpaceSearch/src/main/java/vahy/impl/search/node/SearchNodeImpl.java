package vahy.impl.search.node;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.node.AbstractSearchNode;

import java.util.Map;

public class SearchNodeImpl<
        TAction extends Action,
        TReward extends Reward,
        TObservation extends Observation,
        TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward>,
        TState extends State<TAction, TReward, TObservation>>
        extends AbstractSearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    private final Map<TAction, SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> childNodeMap;

    protected SearchNodeImpl(
            TState wrappedState,
            TSearchNodeMetadata searchNodeMetadata,
            Map<TAction, SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> childNodeMap) {
        super(wrappedState, null, searchNodeMetadata);
        this.childNodeMap = childNodeMap;
    }

    @Override
    public Map<TAction, SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> getChildNodeMap() {
        return childNodeMap;
    }

    @Override
    public void updateChildMap(TAction action, SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> child) {
        childNodeMap.put(action, child);
    }

    @Override
    public StateRewardReturn<TReward, State<TAction, TReward, TObservation>> applyAction(TAction action) {
        return getWrappedState().applyAction(action);
    }

    @Override
    public boolean isLeaf() {
        return isFinalNode() || childNodeMap.isEmpty();
    }
}
