package vahy.impl.search.MCTS.ucb1;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadata;
import vahy.utils.ImmutableTuple;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.SplittableRandom;

public class Ucb1ExpectedRewardNormalizingNodeSelector<
    TAction extends Action,
    TReward extends DoubleScalarReward,
    TObservation extends Observation,
    TState extends State<TAction, TReward, TObservation, TState>>
    extends Ucb1NodeSelector<TAction, TReward, TObservation, TState> {

    public Ucb1ExpectedRewardNormalizingNodeSelector(SplittableRandom random, double explorationConstant) {
        super(random, explorationConstant);
    }

    @Override
    protected TAction getBestAction(SearchNode<TAction, TReward, TObservation, MonteCarloTreeSearchMetadata<TReward>, TState> node) {
        double sum = node
            .getChildNodeStream()
            .mapToDouble(x -> x.getSearchNodeMetadata().getExpectedReward().getValue())
            .sum();
        return node
            .getChildNodeStream()
            .map(
                childNode ->
                {
                    MonteCarloTreeSearchMetadata<TReward> childSearchNodeMetadata = childNode.getSearchNodeMetadata();
                    return new ImmutableTuple<>(
                        childNode.getAppliedAction(),
                        calculateUCBValue(
                            (node.isPlayerTurn() ? 1 : -1) * childSearchNodeMetadata.getExpectedReward().getValue() / sum,
                            explorationConstant,
                            node.getSearchNodeMetadata().getVisitCounter(),
                            childSearchNodeMetadata.getVisitCounter())
                        );
                })
            .collect(StreamUtils.toRandomizedMaxCollector(
                Comparator.comparing(ImmutableTuple::getSecond), random))
            .getFirst();
    }
}
