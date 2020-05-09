package vahy.impl.search.nodeSelector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;

import java.util.SplittableRandom;

public class RandomNodeSelector<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends vahy.impl.search.node.nodeMetadata.BaseSearchNodeMetadata,
    TState extends State<TAction, TObservation, TState>>
    extends AbstractTreeBasedNodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> {

    private final SplittableRandom random;

    public RandomNodeSelector(SplittableRandom random) {
        this.random = random;
    }

    @Override
    public SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectNextNode() {
        checkRoot();
        SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node = root;
        while(!node.isLeaf()) {
            TAction[] allPossibleActions = node.getAllPossibleActions();
            var action = allPossibleActions[random.nextInt(allPossibleActions.length)];
            node = node.getChildNodeMap().get(action);
        }
        return node;
    }
}
