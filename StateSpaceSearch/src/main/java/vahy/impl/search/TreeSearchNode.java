package vahy.impl.search;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;
import vahy.api.search.SearchNode;

public class TreeSearchNode<TAction extends Action, TObservation extends Observation, TState extends State<TAction, ? extends Reward, TObservation>> extends AbstractSearchNode<TAction, TObservation, TState> {

    protected TreeSearchNode(TState wrappedState) {
        super(wrappedState);
    }

    @Override
    public SearchNode<TAction, TObservation, TState> applyAction(TAction action) {
        return new TreeSearchNode<TAction, TObservation, TState>(getWrappedState().applyAction(action));
    }

    @Override
    public int compareTo(SearchNode<TAction, TObservation, TState> o) {
        return 0;
    }
}
