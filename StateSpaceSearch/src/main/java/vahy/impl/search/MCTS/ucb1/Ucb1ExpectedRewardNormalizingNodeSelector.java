package vahy.impl.search.MCTS.ucb1;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadata;
import vahy.utils.ImmutableTuple;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.SplittableRandom;

public class Ucb1ExpectedRewardNormalizingNodeSelector<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends Ucb1NodeSelector<TAction, TPlayerObservation, TOpponentObservation, TState> {

    public Ucb1ExpectedRewardNormalizingNodeSelector(SplittableRandom random, double explorationConstant) {
        super(random, explorationConstant);
    }

    @Override
    protected TAction getBestAction(SearchNode<TAction, TPlayerObservation, TOpponentObservation, MonteCarloTreeSearchMetadata, TState> node) {
        double sum = node
            .getChildNodeStream()
            .mapToDouble(x -> x.getSearchNodeMetadata().getExpectedReward())
            .sum();
        return node
            .getChildNodeStream()
            .map(
                childNode ->
                {
                    MonteCarloTreeSearchMetadata childSearchNodeMetadata = childNode.getSearchNodeMetadata();
                    return new ImmutableTuple<>(
                        childNode.getAppliedAction(),
                        calculateUCBValue(
                            (node.isPlayerTurn() ? 1 : -1) * childSearchNodeMetadata.getExpectedReward() / sum,
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
