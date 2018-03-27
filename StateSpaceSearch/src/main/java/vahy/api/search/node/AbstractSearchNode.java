package vahy.api.search.node;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;

public abstract class AbstractSearchNode<
        TAction extends Action,
        TReward extends Reward,
        TObservation extends Observation,
        TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward>,
        TState extends State<TAction, TReward,TObservation>>
        implements SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    private final TState wrappedState;
    private final TSearchNodeMetadata searchNodeMetadata;
    private final SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> parent;

    protected AbstractSearchNode(
            TState wrappedState,
            SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> parent,
            TSearchNodeMetadata searchNodeMetadata) {
        this.wrappedState = wrappedState;
        this.searchNodeMetadata = searchNodeMetadata;
        this.parent = parent;
    }

    @Override
    public SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> getParent() {
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
}
