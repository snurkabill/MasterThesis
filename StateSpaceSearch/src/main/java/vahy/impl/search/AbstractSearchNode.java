package vahy.impl.search;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;
import vahy.api.search.SearchNode;

public abstract class AbstractSearchNode<TAction extends Action, TObservation extends Observation, TState extends State<TAction, ? extends Reward, TObservation>> implements SearchNode<TAction, TObservation, TState> {

    private final TState wrappedState;

    protected AbstractSearchNode(TState wrappedState) {
        this.wrappedState = wrappedState;
    }

    @Override
    public TState getWrappedState() {
        return wrappedState;
    }
}
