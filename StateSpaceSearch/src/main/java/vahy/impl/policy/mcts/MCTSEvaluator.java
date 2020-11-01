package vahy.impl.policy.mcts;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.AbstractNodeEvaluator;
import vahy.utils.ImmutableTuple;

public abstract class MCTSEvaluator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends MCTSMetadata,
    TState extends State<TAction, TObservation, TState>>
    extends AbstractNodeEvaluator<TAction, TObservation, TSearchNodeMetadata, TState> {

    public MCTSEvaluator(SearchNodeFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeFactory) {
        super(searchNodeFactory);
    }

    @Override
    protected int evaluateNode_inner(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectedNode) {
        var estimatesWithNodeCount = estimateRewards(selectedNode);
        var estimatedRewards = estimatesWithNodeCount.getFirst();
        var expectedRewards = selectedNode.getSearchNodeMetadata().getExpectedReward();
        System.arraycopy(estimatedRewards, 0, expectedRewards, 0, expectedRewards.length);
        return estimatesWithNodeCount.getSecond();
    }

    protected abstract ImmutableTuple<double[], Integer> estimateRewards(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectedNode);
}
