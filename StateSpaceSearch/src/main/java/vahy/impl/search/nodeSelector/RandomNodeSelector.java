package vahy.impl.search.nodeSelector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;

import java.util.SplittableRandom;

public class RandomNodeSelector<
    TAction extends Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends vahy.impl.search.node.nodeMetadata.BaseSearchNodeMetadata,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractTreeBasedNodeSelector<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private final SplittableRandom random;

    public RandomNodeSelector(SplittableRandom random) {
        this.random = random;
    }

    @Override
    protected TAction getBestAction(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node) {
        TAction[] allPossibleActions = node.getAllPossibleActions();
        return allPossibleActions[random.nextInt(allPossibleActions.length)];
    }
}
