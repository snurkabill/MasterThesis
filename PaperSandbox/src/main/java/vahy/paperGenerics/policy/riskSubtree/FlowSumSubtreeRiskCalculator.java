package vahy.paperGenerics.policy.riskSubtree;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.PaperStateWrapper;
import vahy.paperGenerics.metadata.PaperMetadata;

import java.util.ArrayDeque;

public class FlowSumSubtreeRiskCalculator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    implements SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState> {

    @Override
    public double calculateRisk(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> subTreeRoot) {
        double risk = 0;
        int inGameEntityId = subTreeRoot.getStateWrapper().getInGameEntityId();
        var queue = new ArrayDeque<SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>>();
        queue.addFirst(subTreeRoot);
        while(!queue.isEmpty()) {
            var node = queue.poll();
            if(node.isLeaf()) {
                if(node.isFinalNode()) {
                    risk += ((PaperStateWrapper<TAction, TObservation, TState>)node.getStateWrapper()).isRiskHit() ? node.getSearchNodeMetadata().getFlow() : 0.0;
                } else {
                    risk += node.getSearchNodeMetadata().getExpectedRisk()[inGameEntityId] * node.getSearchNodeMetadata().getFlow();
                }
            } else {
                for (var entry : node.getChildNodeMap().entrySet()) {
                    queue.addLast(entry.getValue());
                }
            }
        }
        return risk / subTreeRoot.getSearchNodeMetadata().getFlow(); // dividing risk by flow in the root since flow might be smaller than 1.0 and we want to return risk as we already were in subtree.
    }

    @Override
    public String toLog() {
        return "SUBTREE_RISK_OPTIMIZED_FLOW";
    }

}
