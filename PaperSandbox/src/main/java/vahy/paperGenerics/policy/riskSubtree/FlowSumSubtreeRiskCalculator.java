package vahy.paperGenerics.policy.riskSubtree;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.PaperState;

import java.util.LinkedList;

public class FlowSumSubtreeRiskCalculator<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    @Override
    public double calculateRisk(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> subTreeRoot) {
        double risk = 0;
        var queue = new LinkedList<SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>>();
        queue.addFirst(subTreeRoot);
        while(!queue.isEmpty()) {
            var node = queue.poll();
            if(node.isLeaf()) {
                if(node.isFinalNode()) {
                    risk += node.getWrappedState().isRiskHit() ? node.getSearchNodeMetadata().getNodeProbabilityFlow().getSolution() : 0.0;
                } else {
                    risk += node.getSearchNodeMetadata().getPredictedRisk() * node.getSearchNodeMetadata().getNodeProbabilityFlow().getSolution();
                }
            } else {
                for (var entry : node.getChildNodeMap().entrySet()) {
                    queue.addLast(entry.getValue());
                }
            }
        }
        return risk;
    }

}
