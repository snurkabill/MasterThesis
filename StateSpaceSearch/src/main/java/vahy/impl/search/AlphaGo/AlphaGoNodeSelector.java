package vahy.impl.search.AlphaGo;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.search.nodeSelector.AbstractTreeBasedNodeSelector;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.SplittableRandom;

public class AlphaGoNodeSelector<
    TAction extends Action,
    TReward extends DoubleReward,
    TObservation extends DoubleVectorialObservation,
    TState extends State<TAction, TReward, TObservation, TState>>
    extends AbstractTreeBasedNodeSelector<TAction, TReward, TObservation, AlphaGoNodeMetadata<TAction, TReward>, TState> {

    private final double cpuctParameter;
    private final SplittableRandom random;

    public AlphaGoNodeSelector(double cpuctParameter, SplittableRandom random) {
        this.cpuctParameter = cpuctParameter;
        this.random = random;
    }

    @Override
    protected TAction getBestAction(SearchNode<TAction, TReward, TObservation, AlphaGoNodeMetadata<TAction, TReward>, TState> node) {
        AlphaGoNodeMetadata<TAction, TReward> searchNodeMetadata = node.getSearchNodeMetadata();
        int totalNodeVisitCount = searchNodeMetadata.getVisitCounter();
        return node
            .getChildNodeStream()
            .collect(StreamUtils.toRandomizedMaxCollector(
                Comparator.comparing(
                    childNode -> {
                        AlphaGoNodeMetadata<TAction, TReward> childMetadata = childNode.getSearchNodeMetadata();
                        return
                            (node.isPlayerTurn() ? 1.0 : -1.0) * childMetadata.getExpectedReward().getValue() +
                            calculateUValue(totalNodeVisitCount, childMetadata.getPriorProbability(), childMetadata.getVisitCounter());
                    }), random))
            .getAppliedAction();
    }

    private double calculateUValue(int nodeTotalVisitCount, double priorActionProbability, int childTotalVisitCount) {
//        return cpuctParameter * priorActionProbability * Math.sqrt(nodeTotalVisitCount) / (1.0 + childTotalVisitCount);
        return cpuctParameter * priorActionProbability * (Math.sqrt(nodeTotalVisitCount) / childTotalVisitCount);
    }
}
