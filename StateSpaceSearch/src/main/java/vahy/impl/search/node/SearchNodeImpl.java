package vahy.impl.search.node;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.AbstractSearchNode;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class SearchNodeImpl<TAction extends Enum<TAction> & Action, TObservation extends Observation, TSearchNodeMetadata extends SearchNodeMetadata, TState extends State<TAction, TObservation, TState>>
    extends AbstractSearchNode<TAction, TObservation, TSearchNodeMetadata, TState> {

    public static AtomicLong nodeInstanceId = new AtomicLong(0);
    public final long nodeId = nodeInstanceId.getAndIncrement();
    private final Map<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> childNodeMap;

    public SearchNodeImpl(StateWrapper<TAction, TObservation, TState> wrappedState, TSearchNodeMetadata searchNodeMetadata, Map<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> childNodeMap) {
        this(wrappedState, searchNodeMetadata, childNodeMap, null, null);
    }

    public SearchNodeImpl(
        StateWrapper<TAction, TObservation, TState> wrappedState,
        TSearchNodeMetadata searchNodeMetadata,
        Map<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> childNodeMap,
        SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> parent,
        TAction appliedAction) {
        super(wrappedState, parent, appliedAction, searchNodeMetadata);
        this.childNodeMap = childNodeMap;
    }

    @Override
    public Map<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> getChildNodeMap() {
        return childNodeMap;
    }

    @Override
    public StateWrapperRewardReturn<TAction, TObservation, TState> applyAction(TAction action) {
        return wrappedState.applyAction(action);
    }

    @Override
    public String toString() {
        String metadataString = this.getSearchNodeMetadata().toString();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\\nnodeId: ");
        stringBuilder.append(nodeId);
        stringBuilder.append(metadataString);
        stringBuilder.append("\\nisLeaf: ");
        stringBuilder.append(isLeaf());
        return stringBuilder.toString();
    }
}
