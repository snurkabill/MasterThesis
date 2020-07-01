package vahy.paperGenerics.policy.riskSubtree;

import vahy.paperGenerics.PaperStateWrapper;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;

import java.util.LinkedList;

public class FlowSumSubtreeRiskCalculator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    implements SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState> {

    @Override
    public double calculateRisk(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> subTreeRoot) {
        double risk = 0;
        var queue = new LinkedList<SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>>();
        queue.addFirst(subTreeRoot);
        while(!queue.isEmpty()) {
            var node = queue.poll();
            if(node.isLeaf()) {
                if(node.isFinalNode()) {
                    risk += ((PaperStateWrapper<TAction, TObservation, TState>)node.getStateWrapper()).isRiskHit() ? node.getSearchNodeMetadata().getFlow() : 0.0;
                } else {
                    risk += node.getSearchNodeMetadata().getExpectedRisk() * node.getSearchNodeMetadata().getFlow();
                }
            } else {
                for (var entry : node.getChildNodeMap().entrySet()) {
                    queue.addLast(entry.getValue());
                }
            }
        }
        return risk;
    }

    @Override
    public String toLog() {
        return "SUBTREE_RISK_OPTIMIZED_FLOW";
    }

}
