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
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata<TReward>,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractSearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    public static long nodeInstanceId = 0;
    private final long nodeId = nodeInstanceId;
    private final Map<TAction, SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> childNodeMap;

    public SearchNodeImpl(
        TState wrappedState,
        TSearchNodeMetadata searchNodeMetadata,
        Map<TAction, SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> childNodeMap) {
        this(wrappedState, searchNodeMetadata, childNodeMap, null, null);
    }

    public SearchNodeImpl(
        TState wrappedState,
        TSearchNodeMetadata searchNodeMetadata,
        Map<TAction, SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> childNodeMap,
        SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> parent,
        TAction appliedAction) {
        super(wrappedState, parent, appliedAction, searchNodeMetadata);
        this.childNodeMap = childNodeMap;
        nodeInstanceId++;
    }

    @Override
    public Map<TAction, SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> getChildNodeMap() {
        return childNodeMap;
    }

    @Override
    public StateRewardReturn<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> applyAction(TAction action) {
        return wrappedState.applyAction(action);
    }

    @Override
    public boolean isLeaf() {
        return childNodeMap.isEmpty() || isFinalState;
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
