package vahy.impl.policy.maximizingEstimatedReward;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.impl.policy.AbstractTreeSearchPolicy;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.impl.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.SplittableRandom;

public abstract class AbstractEstimatedRewardMaximizingTreeSearchPolicy<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata<TReward>,
    TState extends State<TAction, TReward, TObservation, TState>>
    extends AbstractTreeSearchPolicy<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    private final SplittableRandom random;

    public AbstractEstimatedRewardMaximizingTreeSearchPolicy(SplittableRandom random,
                                                             TreeUpdateCondition treeUpdateCondition,
                                                             SearchTreeImpl<TAction, TReward, TObservation, TSearchNodeMetadata, TState> searchTree) {
        super(treeUpdateCondition, searchTree);
        this.random = random;
    }

    @Override
    public TAction getDiscreteAction(TState gameState) {
        expandSearchTree(gameState);
        return searchTree
            .getRoot()
            .getChildNodeMap()
            .entrySet()
            .stream()
            .collect(StreamUtils.toRandomizedMaxCollector(Comparator.comparing(o -> o.getValue().getSearchNodeMetadata().getExpectedReward()), random))
            .getKey();
    }

    @Override
    public double[] getActionProbabilityDistribution(TState gameState) {
        throw new UnsupportedOperationException("I will implement this when it will be needed.");
    }

}
