package vahy.impl.search.AlphaGo;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.search.nodeSelector.AbstractTreeBasedNodeSelector;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.SplittableRandom;

public class AlphaGoNodeSelector<
    TAction extends Action<TAction>,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends DoubleVector,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractTreeBasedNodeSelector<TAction, TPlayerObservation, TOpponentObservation, AlphaGoNodeMetadata<TAction>, TState> {

    private final double cpuctParameter;
    private final SplittableRandom random;

    public AlphaGoNodeSelector(double cpuctParameter, SplittableRandom random) {
        this.cpuctParameter = cpuctParameter;
        this.random = random;
    }

    private double calculateUValue(int nodeTotalVisitCount, double priorActionProbability, int childTotalVisitCount) {
//        return cpuctParameter * priorActionProbability * Math.sqrt(nodeTotalVisitCount) / (1.0 + childTotalVisitCount);
        return cpuctParameter * priorActionProbability * (Math.sqrt(nodeTotalVisitCount / (double)childTotalVisitCount));
    }

    @Override
    public SearchNode<TAction, TPlayerObservation, TOpponentObservation, AlphaGoNodeMetadata<TAction>, TState> selectNextNode() {
        checkRoot();
        var node = root;
        while(!node.isLeaf()) {
            var searchNodeMetadata = node.getSearchNodeMetadata();
            int totalNodeVisitCount = searchNodeMetadata.getVisitCounter();
            var isplayerTurn = node.isPlayerTurn();
            var action = node
                .getChildNodeStream()
                .collect(StreamUtils.toRandomizedMaxCollector(
                    Comparator.comparing(
                        childNode -> {
                            AlphaGoNodeMetadata<TAction> childMetadata = childNode.getSearchNodeMetadata();
                            return
                                (isplayerTurn ? 1.0 : -1.0) * childMetadata.getExpectedReward() +
                                calculateUValue(totalNodeVisitCount, childMetadata.getPriorProbability(), childMetadata.getVisitCounter());
                        }), random))
                .getAppliedAction();
            node = node.getChildNodeMap().get(action);
        }
        return node;
    }
}
