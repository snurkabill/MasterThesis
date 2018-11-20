package vahy.impl.policy.maximizingEstimatedReward;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
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
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>> extends AbstractTreeSearchPolicy<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata> {

    private final SplittableRandom random;

    public AbstractEstimatedRewardMaximizingTreeSearchPolicy(SplittableRandom random,
                                                             TreeUpdateCondition treeUpdateCondition,
                                                             SearchTreeImpl<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, State<TAction, TReward, TObservation>> searchTree) {
        super(treeUpdateCondition, searchTree);
        this.random = random;
    }

    @Override
    public TAction getDiscreteAction(State<TAction, TReward, TObservation> gameState) {
        expandSearchTree(gameState);
        return searchTree
            .getRoot()
            .getSearchNodeMetadata()
            .getStateActionMetadataMap()
            .entrySet()
            .stream()
            .collect(StreamUtils.toRandomizedMaxCollector(Comparator.comparing(o -> o.getValue().getEstimatedTotalReward()), random))
            .getKey();
    }

    @Override
    public double[] getActionProbabilityDistribution(State<TAction, TReward, TObservation> gameState) {
        throw new UnsupportedOperationException("I will implement this when it will be needed.");
    }

}
