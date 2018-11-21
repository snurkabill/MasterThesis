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

    private final double epsilon;
    private final SplittableRandom random;

    public EGreedyNodeSelector(double epsilon, SplittableRandom random) {
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
                .getChildNodeMap()
                .entrySet()
                .stream()
                .collect(StreamUtils.toRandomizedMaxCollector(
                    Comparator.comparing(
                        o -> o.getValue().getSearchNodeMetadata().getEstimatedTotalReward()), random))
                .getKey();
        }
    }
}
