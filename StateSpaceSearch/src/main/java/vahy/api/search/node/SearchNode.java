package vahy.api.search.node;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;

import java.util.Map;
import java.util.stream.Stream;

public interface SearchNode<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> getParent();

    TAction getAppliedAction();

    Map<TAction, SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> getChildNodeMap();

    StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState> applyAction(TAction action);

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

    default Stream<SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> getChildNodeStream() {
        return getChildNodeMap().values().stream();
    }

}
