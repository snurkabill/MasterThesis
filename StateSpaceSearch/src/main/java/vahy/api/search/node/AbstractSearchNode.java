package vahy.api.search.node;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;

public abstract class AbstractSearchNode<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>>
    implements SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {

    private final TState wrappedState;
    private final TSearchNodeMetadata searchNodeMetadata;
    private SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> parent;
    private TAction appliedParentAction;

    protected AbstractSearchNode(
        TState wrappedState,
        SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> parent,
        TAction appliedParentAction,
        TSearchNodeMetadata searchNodeMetadata) {
        this.wrappedState = wrappedState;
        this.searchNodeMetadata = searchNodeMetadata;
        this.parent = parent;
        this.appliedParentAction = appliedParentAction;
    }

    @Override
    public TAction getAppliedParentAction() {
        return appliedParentAction;
    }

    @Override
    public SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> getParent() {
        return parent;
    }

    @Override
    public TAction[] getAllPossibleActions() {
        return wrappedState.getAllPossibleActions();
    }

    @Override
    public TSearchNodeMetadata getSearchNodeMetadata() {
        return this.searchNodeMetadata;
    }

    @Override
    public TState getWrappedState() {
        return wrappedState;
    }

    @Override
    public boolean isFinalNode() {
        return wrappedState.isFinalState();
    }

    @Override
    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public void makeRoot() {
        parent = null;
        appliedParentAction = null;
    }
}
