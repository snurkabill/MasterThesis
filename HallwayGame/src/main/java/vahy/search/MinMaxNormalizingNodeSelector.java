package vahy.search;

import vahy.api.search.node.SearchNode;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadata;
import vahy.impl.search.nodeSelector.AbstractTreeBasedNodeSelector;
import vahy.utils.ImmutableTuple;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.SplittableRandom;

public class MinMaxNormalizingNodeSelector extends AbstractTreeBasedNodeSelector<ActionType, DoubleReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleReward>, ImmutableStateImpl> {

    public static final double TOLERANCE = 0.0000000001;

    private final SplittableRandom random;
    private final double cpuctParameter;

    public MinMaxNormalizingNodeSelector(SplittableRandom random, double cpuctParameter) {
        this.random = random;
        this.cpuctParameter = cpuctParameter;
    }

    @Override
    protected ActionType getBestAction(SearchNode<ActionType, DoubleReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleReward>, ImmutableStateImpl> node) {
        int totalNodeVisitCount = node.getSearchNodeMetadata().getVisitCounter();

        double max = node
            .getChildNodeStream()
            .mapToDouble(x -> x.getSearchNodeMetadata().getExpectedReward().getValue())
            .max().orElseThrow(() -> new IllegalStateException("Maximum Does not exists"));

        double min = node
            .getChildNodeStream()
            .mapToDouble(x -> x.getSearchNodeMetadata().getExpectedReward().getValue())
            .min().orElseThrow(() -> new IllegalStateException("Maximum Does not exists"));


        final double finalMax = max;
        final double finalMin = min;

        double priorProbability = 1.0 / node.getChildNodeMap().size();

        return node
            .getChildNodeStream()
            .map(x -> {
                ActionType action = x.getAppliedAction();
                double uValue = calculateUValue(priorProbability, x.getSearchNodeMetadata().getVisitCounter(), totalNodeVisitCount);
                double qValue = x.getSearchNodeMetadata().getExpectedReward().getValue() == 0 ? 0 :
                    (x.getSearchNodeMetadata().getExpectedReward().getValue() - finalMin) /
                        (Math.abs(finalMax - finalMin) < TOLERANCE ? finalMax : (finalMax - finalMin));

                return new ImmutableTuple<>(action, qValue + uValue);
            })
            .collect(StreamUtils.toRandomizedMaxCollector(Comparator.comparing(ImmutableTuple::getSecond), random))
            .getFirst();
    }

    private double calculateUValue(double priorProbability, int childVisitCount, int nodeTotalVisitCount) {
        return cpuctParameter * priorProbability * Math.sqrt(nodeTotalVisitCount) / (1.0 + childVisitCount);
    }
}
