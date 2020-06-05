package vahy.impl.search.nodeSelector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.NodeMetadata;
import vahy.api.search.node.SearchNode;
import vahy.api.search.nodeSelector.RandomizedNodeSelector;

import java.util.SplittableRandom;

public class RandomNodeSelector<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends NodeMetadata,
    TState extends State<TAction, TObservation, TState>>
    extends RandomizedNodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> {

    public RandomNodeSelector(SplittableRandom random) {
        super(random);
    }

    @Override
    public SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectNextNode(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> root) {
        SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node = root;
        while(!node.isLeaf()) {
            TAction[] allPossibleActions = node.getAllPossibleActions();
            var action = allPossibleActions[random.nextInt(allPossibleActions.length)];
            node = node.getChildNodeMap().get(action);
        }
        return node;
    }
}
