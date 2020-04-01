package vahy.api.search.node;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

public abstract class AbstractSearchNode<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    protected final boolean isFinalState;
    protected final boolean isOpponentTurn;
    protected final TAction[] allPossibleActions;
    protected final TState wrappedState;
    private final TSearchNodeMetadata searchNodeMetadata;
    private SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> parent;
    private TAction appliedParentAction;

    private boolean isLeaf = true;

    protected AbstractSearchNode(
        TState wrappedState,
        SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> parent,
        TAction appliedParentAction,
        TSearchNodeMetadata searchNodeMetadata) {
        this.wrappedState = wrappedState;
        this.searchNodeMetadata = searchNodeMetadata;
        this.parent = parent;
        this.appliedParentAction = appliedParentAction;
        this.isFinalState = wrappedState.isFinalState();
        this.isOpponentTurn = wrappedState.isOpponentTurn();
        this.allPossibleActions = wrappedState.getAllPossibleActions();
    }

    @Override
    public void unmakeLeaf() {
        this.isLeaf = false;
    }

    @Override
    public boolean isLeaf() {
        if(isFinalState && !isLeaf) {
            throw new IllegalStateException("Final state must be leaf");
        }
        return isLeaf;
    }

    @Override
    public TAction getAppliedAction() {
        return appliedParentAction;
    }

    @Override
    public SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> getParent() {
        return parent;
    }

    @Override
    public TAction[] getAllPossibleActions() {
        return allPossibleActions;
    }

    @Override
    public TSearchNodeMetadata getSearchNodeMetadata() {
        return searchNodeMetadata;
    }

    @Override
    public TState getWrappedState() {
        return wrappedState;
    }

    @Override
    public boolean isFinalNode() {
        return isFinalState;
    }

    @Override
    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public boolean isOpponentTurn() {
        return isOpponentTurn;
    }

    @Override
    public void makeRoot() {
        parent = null;
        appliedParentAction = null;
    }
}
