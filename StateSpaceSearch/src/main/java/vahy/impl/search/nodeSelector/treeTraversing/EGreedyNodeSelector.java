package vahy.impl.search.nodeSelector.treeTraversing;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.nodeSelector.RandomizedNodeSelector;
import vahy.impl.policy.mcts.MCTSMetadata;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.SplittableRandom;

public class EGreedyNodeSelector<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends MCTSMetadata,
    TState extends State<TAction, TObservation, TState>>
    extends RandomizedNodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> {

    private class SearchNodeComparator implements Comparator<SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>>{

        @Override
        public int compare(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> o1, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> o2) {
            var inGameEntityIdOnTurn = o1.getStateWrapper().getInGameEntityOnTurnId();
            return Double.compare(o1.getSearchNodeMetadata().getExpectedReward()[inGameEntityIdOnTurn], o2.getSearchNodeMetadata().getExpectedReward()[inGameEntityIdOnTurn]);
        }

    }

    private final double epsilon;

    private final SearchNodeComparator nodeComparator = new SearchNodeComparator();

    public EGreedyNodeSelector(SplittableRandom random, double epsilon) {
        super(random);
        this.epsilon = epsilon;
    }

    @Override
    public SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectNextNode(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> root) {
        SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node = root;
        while(!node.isLeaf()) {
            var action = getAction(node);
            node = node.getChildNodeMap().get(action);
        }
        return node;
    }

    private TAction getAction(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node) {
        if (random.nextDouble() < epsilon) {
            TAction[] allPossibleActions = node.getAllPossibleActions();
            return allPossibleActions[random.nextInt(allPossibleActions.length)];
        } else {
            return node.getChildNodeStream().collect(StreamUtils.toRandomizedMaxCollector(nodeComparator, random)).getAppliedAction();
        }
    }
}
