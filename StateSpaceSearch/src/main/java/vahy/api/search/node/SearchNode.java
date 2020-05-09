package vahy.api.search.node;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;

import java.util.Map;
import java.util.stream.Stream;

public interface SearchNode<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata,
    TState extends State<TAction, TObservation, TState>> {

    SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> getParent();

    TAction getAppliedAction();

    Map<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> getChildNodeMap();

    StateRewardReturn<TAction, TObservation, TState> applyAction(TAction action);

    TAction[] getAllPossibleActions();

    TSearchNodeMetadata getSearchNodeMetadata();

    TState getWrappedState();

    boolean isFinalNode();

    boolean isRoot();

    boolean isLeaf();

    void unmakeLeaf();

    boolean isOpponentTurn();

    void makeRoot();

    default boolean isPlayerTurn() {
        return !isOpponentTurn();
    }

    default Stream<SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> getChildNodeStream() {
        return getChildNodeMap().values().stream();
    }

}
