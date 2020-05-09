package vahy.impl.search.nodeSelector.treeTraversing;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.impl.search.nodeSelector.AbstractTreeBasedNodeSelector;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.SplittableRandom;

public class EGreedyNodeSelector<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata,
    TState extends State<TAction, TObservation, TState>>
    extends AbstractTreeBasedNodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> {

    @Override
    public SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectNextNode() {
        checkRoot();
        SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node = root;
        while(!node.isLeaf()) {
            var action = getAction(node);
            node = node.getChildNodeMap().get(action);
        }
        return node;
    }

    private class SearchNodeComparator implements Comparator<SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>>{

        @Override
        public int compare(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> o1, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> o2) {
            return Double.compare(o1.getSearchNodeMetadata().getExpectedReward(), o2.getSearchNodeMetadata().getExpectedReward());
        }
    }

    private final double epsilon;
    private final SplittableRandom random;
    private final SearchNodeComparator nodeComparator = new SearchNodeComparator();

    public EGreedyNodeSelector(SplittableRandom random, double epsilon) {
        this.epsilon = epsilon;
        this.random = random;
    }

    private TAction getAction(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node) {
        if (random.nextDouble() < epsilon) {
            TAction[] allPossibleActions = node.getAllPossibleActions();
            return allPossibleActions[random.nextInt(allPossibleActions.length)];
        } else {
            return node
                .getChildNodeStream()
                .collect(StreamUtils.toRandomizedMaxCollector(
                    (node.isPlayerTurn() ? nodeComparator : nodeComparator.reversed())
                    , random))
                .getAppliedAction();
        }
    }
}
