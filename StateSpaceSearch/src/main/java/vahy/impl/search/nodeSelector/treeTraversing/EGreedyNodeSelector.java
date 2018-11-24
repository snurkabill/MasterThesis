package vahy.impl.search.nodeSelector.treeTraversing;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.impl.search.nodeSelector.AbstractTreeBasedNodeSelector;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.SplittableRandom;

public class EGreedyNodeSelector<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata<TReward>,
    TState extends State<TAction, TReward, TObservation, TState>>
    extends AbstractTreeBasedNodeSelector<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    private class SearchNodeComparator implements Comparator<SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState>>{

        @Override
        public int compare(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> o1, SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> o2) {
            return o1.getSearchNodeMetadata().getExpectedReward().compareTo(o2.getSearchNodeMetadata().getExpectedReward());
        }
    }

    private final double epsilon;
    private final SplittableRandom random;
    private final SearchNodeComparator nodeComparator = new SearchNodeComparator();

    public EGreedyNodeSelector(SplittableRandom random, double epsilon) {
        this.epsilon = epsilon;
        this.random = random;
    }

    @Override
    protected TAction getBestAction(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> node) {
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
