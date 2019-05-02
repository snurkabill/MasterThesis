package vahy.paperGenerics.policy.riskSubtree;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.utils.ImmutableTuple;

import java.util.LinkedList;

public class SubtreePriorRiskCalculator<
    TAction extends Action,
    TReward extends DoubleReward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    implements SubtreeRiskCalculator<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    @Override
    public double calculateRisk(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> subtreeRoot) {
        double totalRisk = 0;
        var queue = new LinkedList<ImmutableTuple<SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>, Double>>();
        queue.add(new ImmutableTuple<>(subtreeRoot, 1.0));
        while(!queue.isEmpty()) {
            var node = queue.poll();
            if(node.getFirst().isLeaf()) {
                if(node.getFirst().isFinalNode()) {
                    totalRisk += node.getFirst().getWrappedState().isRiskHit() ? node.getSecond() : 0.0;
                } else {
                    totalRisk += node.getSecond() * node.getFirst().getSearchNodeMetadata().getPredictedRisk();
                }
            } else {
                for (var entry : node.getFirst().getChildNodeMap().entrySet()) {
                    queue.addLast(new ImmutableTuple<>(entry.getValue(), entry.getValue().getSearchNodeMetadata().getPriorProbability() * node.getSecond()));
                }
            }
        }
        return totalRisk;
    }
}
