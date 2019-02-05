package vahy.paperGenerics;

import org.jetbrains.annotations.NotNull;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.search.nodeSelector.AbstractTreeBasedNodeSelector;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;
import vahy.utils.StreamUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.OptionalDouble;
import java.util.SplittableRandom;
import java.util.function.Function;
import java.util.stream.DoubleStream;

public class PaperNodeSelector<
    TAction extends Action,
    TReward extends DoubleReward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractTreeBasedNodeSelector<TAction, TReward, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction, TReward>, TState> {

    private final double cpuctParameter;
    protected final SplittableRandom random;

    public PaperNodeSelector(double cpuctParameter, SplittableRandom random) {
        this.cpuctParameter = cpuctParameter;
        this.random = random;
    }

    protected final ImmutableTuple<Double, Double> getMinMax(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction, TReward>, TState> node) {
        double helpMax = -Double.MAX_VALUE;
        double helpMin = Double.MAX_VALUE;

        for (SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction, TReward>, TState> entry : node.getChildNodeMap().values()) {
            double value = entry.getSearchNodeMetadata().getExpectedReward().getValue() + entry.getSearchNodeMetadata().getGainedReward().getValue();
            if(helpMax < value) {
                helpMax = value;
            }
            if(helpMin > value) {
                helpMin = value;
            }
        }
        return new ImmutableTuple<>(helpMin, helpMax);
    }

    protected final double getExtremeElement(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction, TReward>, TState> node,
                                     Function<DoubleStream, OptionalDouble> function,
                                     String nonExistingElementMessage) {
        return function.apply(node
            .getChildNodeStream()
            .mapToDouble(x -> x.getSearchNodeMetadata().getExpectedReward().getValue())
        ).orElseThrow(() -> new IllegalStateException(nonExistingElementMessage));
    }

    protected final TAction sampleOpponentAction(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction, TReward>, TState> node) {
        var actions = new ArrayList<TAction>();
        var priorProbabilities = new double[node.getChildNodeMap().size()];
        int index = 0;
        for(var entry : node.getChildNodeMap().values()) {
            actions.add(entry.getAppliedAction());
            priorProbabilities[index] = entry.getSearchNodeMetadata().getPriorProbability();
            index++;
        }
        int randomIndex = RandomDistributionUtils.getRandomIndexFromDistribution(priorProbabilities, random);
        return actions.get(randomIndex);
    }

    @Override
    protected TAction getBestAction(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction, TReward>, TState> node) {
        if(!node.isOpponentTurn()) {
            int totalNodeVisitCount = node.getSearchNodeMetadata().getVisitCounter();

//            final double max = getExtremeElement(node, DoubleStream::max, "Maximum Does not exists");
//            final double min = getExtremeElement(node, DoubleStream::min, "Minimum Does not exists");

            double maxHelp = -Double.MAX_VALUE;
            double minHelp = Double.MAX_VALUE;

            var childNodeMap = node.getChildNodeMap();
            for (var entry : childNodeMap.values()) {
                double value = entry.getSearchNodeMetadata().getExpectedReward().getValue() + entry.getSearchNodeMetadata().getGainedReward().getValue();
                if(maxHelp < value) {
                    maxHelp = value;
                }
                if(minHelp > value) {
                    minHelp = value;
                }
            }

            double max = maxHelp;
            double min = minHelp;

            return node
                .getChildNodeStream()
                .map(x -> {
                    var metadata = x.getSearchNodeMetadata();
                    TAction action = x.getAppliedAction();
                    double uValue = calculateUValue(metadata.getPriorProbability(), metadata.getVisitCounter(), totalNodeVisitCount);
                    double qValue = max == min
                        ? 0.5
                        : (((metadata.getExpectedReward().getValue() + metadata.getGainedReward().getValue()) - min) / (max - min));
                    return new ImmutableTuple<>(action, qValue + uValue);
                })
                .collect(StreamUtils.toRandomizedMaxCollector(Comparator.comparing(ImmutableTuple::getSecond), random))
                .getFirst();
        } else {
//
//            Map<TAction, Integer> justTempMap = new HashMap<>();
//            for (int i = 0; i < 100000; i++) {
//                TAction sampledAction = sampleOpponentAction(node);
//                if(justTempMap.containsKey(sampledAction)) {
//                    justTempMap.put(sampledAction, justTempMap.get(sampledAction) + 1);
//                } else {
//                    justTempMap.put(sampledAction, 1);
//                }
//            }

            return sampleOpponentAction(node);
        }
    }

    @NotNull
    protected final Function<
        SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction, TReward>, TState>,
        ImmutableTuple<TAction, Double>>
    getSearchNodeImmutableTupleFunction(final int totalNodeVisitCount, final double min, final double max)
    {
        return x -> {
            var metadata = x.getSearchNodeMetadata();
            TAction action = x.getAppliedAction();
            double uValue = calculateUValue(metadata.getPriorProbability(), metadata.getVisitCounter(), totalNodeVisitCount);
            double qValue = max == min
                ? 0.5
                : (((metadata.getExpectedReward().getValue() + metadata.getGainedReward().getValue()) - min) / (max - min));
            return new ImmutableTuple<>(action, qValue + uValue);
        };
    }

    protected double calculateUValue(double priorProbability, int childVisitCount, int nodeTotalVisitCount) {
        return cpuctParameter * priorProbability * Math.sqrt(nodeTotalVisitCount / (1.0 + childVisitCount));
    }
}
