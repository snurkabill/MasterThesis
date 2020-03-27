package vahy.impl.search.node;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.AbstractSearchNode;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class SearchNodeImpl<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractSearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    public static AtomicLong nodeInstanceId = new AtomicLong(0);
    private final long nodeId = nodeInstanceId.getAndIncrement();
    private final Map<TAction, SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> childNodeMap;

    public SearchNodeImpl(
        TState wrappedState,
        TSearchNodeMetadata searchNodeMetadata,
        Map<TAction, SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> childNodeMap) {
        this(wrappedState, searchNodeMetadata, childNodeMap, null, null);
    }

    public SearchNodeImpl(
        TState wrappedState,
        TSearchNodeMetadata searchNodeMetadata,
        Map<TAction, SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> childNodeMap,
        SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> parent,
        TAction appliedAction) {
        super(wrappedState, parent, appliedAction, searchNodeMetadata);
        this.childNodeMap = childNodeMap;
    }

    @Override
    public Map<TAction, SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> getChildNodeMap() {
        return childNodeMap;
    }

    @Override
    public StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState> applyAction(TAction action) {
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
