package vahy.api.search.node;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.model.observation.Observation;

public abstract class AbstractSearchNode<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TMetadata extends NodeMetadata, TState extends State<TAction, TObservation, TState>>
    implements SearchNode<TAction, TObservation, TMetadata, TState> {

    protected final boolean isFinalState;
    protected final boolean isOpponentTurn;
    protected final TAction[] allPossibleActions;
    protected final StateWrapper<TAction, TObservation, TState> wrappedState;
    private final TMetadata searchNodeMetadata;
    private SearchNode<TAction, TObservation, TMetadata, TState> parent;
    private TAction appliedParentAction;

    private boolean isLeaf = true;

    protected AbstractSearchNode(StateWrapper<TAction, TObservation, TState> wrappedState, SearchNode<TAction, TObservation, TMetadata, TState> parent, TAction appliedParentAction, TMetadata nodeMetadata) {
        this.wrappedState = wrappedState;
        this.searchNodeMetadata = nodeMetadata;
        this.parent = parent;
        this.appliedParentAction = appliedParentAction;
        this.isFinalState = wrappedState.isFinalState();
        this.isOpponentTurn = !wrappedState.isPlayerTurn();
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
    public SearchNode<TAction, TObservation, TMetadata, TState> getParent() {
        return parent;
    }


    @Override
    public TAction[] getAllPossibleActions() {
        return allPossibleActions;
    }

    @Override
    public TMetadata getSearchNodeMetadata() {
        return searchNodeMetadata;
    }

    @Override
    public StateWrapper<TAction, TObservation, TState> getStateWrapper() {
        return wrappedState;
    }

    @Override
    public StateWrapperRewardReturn<TAction, TObservation, TState> applyAction(TAction action) {
        return wrappedState.applyAction(action);
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
