package vahy.impl.search.nodeSelector.treeTraversing.ucb1;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.node.nodeMetadata.MCTSNodeMetadata;
import vahy.utils.ImmutableTuple;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.OptionalDouble;
import java.util.SplittableRandom;
import java.util.function.Function;
import java.util.stream.DoubleStream;

public class Ucb1MinMaxExplorationConstantNodeSelector<
    TAction extends Action,
    TReward extends DoubleScalarReward,
    TObservation extends Observation,
    TState extends State<TAction, TReward, TObservation, TState>>
    extends Ucb1NodeSelector<TAction, TReward, TObservation, TState> {

    public Ucb1MinMaxExplorationConstantNodeSelector(SplittableRandom random) {
        super(random, 0.0d);
    }

    private double findExtreme(Function<DoubleStream, OptionalDouble> function,
                               String exceptionMsg,
                               SearchNode<TAction, TReward, TObservation, MCTSNodeMetadata<TReward>, TState> node) {
        return function
            .apply(node
                .getChildNodeStream()
                .mapToDouble(x -> x.getSearchNodeMetadata().getEstimatedTotalReward().getValue())
            ).orElseThrow(() -> new IllegalArgumentException(exceptionMsg));
    }

    @Override
    protected TAction getBestAction(SearchNode<TAction, TReward, TObservation, MCTSNodeMetadata<TReward>, TState> node) {
        double min = findExtreme(DoubleStream::min, "Minimal element was not found", node);
        double max = findExtreme(DoubleStream::max, "Maximal element was not found", node);
        double explorationConstant = (max + min) / 2.0;
        return node
            .getChildNodeStream()
            .map(
                childNode ->
                {
                    MCTSNodeMetadata<TReward> childSearchNodeMetadata = childNode.getSearchNodeMetadata();
                    return new ImmutableTuple<>(
                        childNode.getAppliedAction(),
                        calculateUCBValue(
                            childSearchNodeMetadata.getEstimatedTotalReward().getValue(),
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
