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
    TAction extends Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends Ucb1NodeSelector<TAction, TPlayerObservation, TOpponentObservation, TState> {

    public Ucb1ExpectedRewardNormalizingNodeSelector(SplittableRandom random, double explorationConstant) {
        super(random, explorationConstant);
    }

    @Override
    public SearchNode<TAction, TPlayerObservation, TOpponentObservation, MonteCarloTreeSearchMetadata, TState> selectNextNode() {
        checkRoot();
        var node = root;
        while(!node.isLeaf()) {
            double sum = node
                .getChildNodeStream()
                .mapToDouble(x -> x.getSearchNodeMetadata().getExpectedReward())
                .sum();

            var node2 = node;
            var action = node
                .getChildNodeStream()
                .map(
                    childNode ->
                    {
                        MonteCarloTreeSearchMetadata childSearchNodeMetadata = childNode.getSearchNodeMetadata();
                        return new ImmutableTuple<>(
                            childNode.getAppliedAction(),
                            calculateUCBValue(
                                (node2.isPlayerTurn() ? 1 : -1) * childSearchNodeMetadata.getExpectedReward() / sum,
                                explorationConstant,
                                node2.getSearchNodeMetadata().getVisitCounter(),
                                childSearchNodeMetadata.getVisitCounter())
                        );
                    })
                .collect(StreamUtils.toRandomizedMaxCollector(
                    Comparator.comparing(ImmutableTuple::getSecond), random))
                .getFirst();
            node = node.getChildNodeMap().get(action);
        }
        return node;
    }

}
