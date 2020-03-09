package vahy.impl.search.MCTS.ucb1;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadata;
import vahy.utils.ImmutableTuple;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.OptionalDouble;
import java.util.SplittableRandom;
import java.util.function.Function;
import java.util.stream.DoubleStream;

public class Ucb1MinMaxExplorationConstantNodeSelector<
    TAction extends Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends Ucb1NodeSelector<TAction, TPlayerObservation, TOpponentObservation, TState> {

    public Ucb1MinMaxExplorationConstantNodeSelector(SplittableRandom random) {
        super(random, 0.0d);
    }

    private double findExtreme(Function<DoubleStream, OptionalDouble> function,
                               String exceptionMsg,
                               SearchNode<TAction, TPlayerObservation, TOpponentObservation, MonteCarloTreeSearchMetadata, TState> node) {
        return function
            .apply(node
                .getChildNodeStream()
                .mapToDouble(x -> x.getSearchNodeMetadata().getExpectedReward())
            ).orElseThrow(() -> new IllegalArgumentException(exceptionMsg));
    }

    @Override
    public SearchNode<TAction, TPlayerObservation, TOpponentObservation, MonteCarloTreeSearchMetadata, TState> selectNextNode() {
        checkRoot();
        var node = root;
        while(!node.isLeaf()) {
            double min = findExtreme(DoubleStream::min, "Minimal element was not found", node);
            double max = findExtreme(DoubleStream::max, "Maximal element was not found", node);
            double explorationConstant = (max + min) / 2.0;
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
                                (node2.isPlayerTurn() ? 1 : -1) * childSearchNodeMetadata.getExpectedReward(),
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
