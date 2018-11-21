package vahy.api.search.node;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;

import java.util.Map;
import java.util.stream.Stream;

public interface SearchNode<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata<TReward>,
    TState extends State<TAction, TReward, TObservation, TState>> {

    SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> getParent();

    TAction getAppliedAction();

    Map<TAction, SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> getChildNodeMap();

    StateRewardReturn<TAction, TReward, TObservation, TState> applyAction(TAction action);

    TAction[] getAllPossibleActions();

    TSearchNodeMetadata getSearchNodeMetadata();

    TState getWrappedState();

    boolean isFinalNode();

    boolean isRoot();

    boolean isLeaf();

    boolean isOpponentTurn();

    void makeRoot();

    default boolean isPlayerTurn() {
        return !isOpponentTurn();
    }

    default Stream<SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> getChildNodeStream() {
        return getChildNodeMap().values().stream();
    }

}
