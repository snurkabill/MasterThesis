package vahy.api.search.node;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;

import java.util.Map;

public interface SearchNode<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>> {

    SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> getParent();

    TAction getAppliedParentAction();

    Map<TAction, SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState>> getChildNodeMap();

    void updateChildMap(TAction action, SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> child);

    StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> applyAction(TAction action);

    TAction[] getAllPossibleActions();

    TSearchNodeMetadata getSearchNodeMetadata();

    State<TAction, TReward, TObservation> getWrappedState();

    boolean isFinalNode();

    boolean isRoot();

    boolean isLeaf();

    boolean isOpponentTurn();

    void makeRoot();

    // TODO: add getting (avg?) reward
}
