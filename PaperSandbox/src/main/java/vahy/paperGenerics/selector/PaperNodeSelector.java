package vahy.paperGenerics.selector;

import org.jetbrains.annotations.NotNull;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.impl.search.nodeSelector.AbstractTreeBasedNodeSelector;
import vahy.paperGenerics.PaperMetadata;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.SplittableRandom;
import java.util.function.Function;
import java.util.stream.DoubleStream;

public class PaperNodeSelector<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractTreeBasedNodeSelector<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> {

    private final double cpuctParameter;
    protected final SplittableRandom random;

    public PaperNodeSelector(double cpuctParameter, SplittableRandom random) {
        this.cpuctParameter = cpuctParameter;
        this.random = random;
    }

    protected final ImmutableTuple<Double, Double> getMinMax(SearchNode<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> node) {
        double helpMax = -Double.MAX_VALUE;
        double helpMin = Double.MAX_VALUE;

        for (SearchNode<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> entry : node.getChildNodeMap().values()) {
            double value = entry.getSearchNodeMetadata().getExpectedReward() + entry.getSearchNodeMetadata().getGainedReward();
            if(helpMax < value) {
                helpMax = value;
            }
            if(helpMin > value) {
                helpMin = value;
            }
        }
        return new ImmutableTuple<>(helpMin, helpMax);
    }

    protected final double getExtremeElement(SearchNode<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> node,
                                     Function<DoubleStream, OptionalDouble> function,
                                     String nonExistingElementMessage) {
        return function.apply(node
            .getChildNodeStream()
            .mapToDouble(x -> x.getSearchNodeMetadata().getExpectedReward())
        ).orElseThrow(() -> new IllegalStateException(nonExistingElementMessage));
    }

    protected final TAction sampleOpponentAction(SearchNode<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> node) {
        var actions = new ArrayList<TAction>(node.getChildNodeMap().size());
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
    protected TAction getBestAction(SearchNode<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> node) {
        if(!node.isOpponentTurn()) {
            int totalNodeVisitCount = node.getSearchNodeMetadata().getVisitCounter();
            double max = -Double.MAX_VALUE;
            double min = Double.MAX_VALUE;
            var childNodeMap = node.getChildNodeMap();
            for (var entry : childNodeMap.values()) {
                double value = entry.getSearchNodeMetadata().getExpectedReward() + entry.getSearchNodeMetadata().getGainedReward();
                if(max < value) {
                    max = value;
                }
                if(min > value) {
                    min = value;
                }
            }

            TAction[] possibleActions = node.getAllPossibleActions();
            var searchNodeMap = node.getChildNodeMap();

            int maxIndex = -1;
            double maxValue = -Double.MAX_VALUE;
            var indexList = new ArrayList<Integer>();

            for (int i = 0; i < possibleActions.length; i++) {
                var metadata = searchNodeMap.get(possibleActions[i]).getSearchNodeMetadata();
                var quValue =
                    calculateUValue(metadata.getPriorProbability(), metadata.getVisitCounter(), totalNodeVisitCount) +
                        (max == min ? 0.5
                            : (((metadata.getExpectedReward() + metadata.getGainedReward()) - min) / (max - min)));
                if(quValue > maxValue) {
                    maxIndex = i;
                    maxValue = quValue;
                } else if(quValue == maxValue) {
                    if(indexList.isEmpty()) {
                        indexList.add(maxIndex);
                        indexList.add(i);
                    } else {
                        indexList.add(i);
                    }
                }
            }
            return indexList.isEmpty() ? possibleActions[maxIndex] : possibleActions[indexList.get(random.nextInt(indexList.size()))];
        } else {
            return sampleOpponentAction(node);
        }
    }

    @NotNull
    protected final Function<
        SearchNode<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState>,
        ImmutableTuple<TAction, Double>>
    getSearchNodeImmutableTupleFunction(final int totalNodeVisitCount, final double min, final double max)
    {
        return x -> {
            var metadata = x.getSearchNodeMetadata();
            TAction action = x.getAppliedAction();
            double uValue = calculateUValue(metadata.getPriorProbability(), metadata.getVisitCounter(), totalNodeVisitCount);
            double qValue = max == min
                ? 0.5
                : (((metadata.getExpectedReward() + metadata.getGainedReward()) - min) / (max - min));
            return new ImmutableTuple<>(action, qValue + uValue);
        };
    }

    protected double calculateUValue(double priorProbability, int childVisitCount, int nodeTotalVisitCount) {
        return cpuctParameter * priorProbability * Math.sqrt(nodeTotalVisitCount / (1.0 + childVisitCount));
    }
}
