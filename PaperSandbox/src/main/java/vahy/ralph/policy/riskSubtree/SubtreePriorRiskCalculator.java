package vahy.ralph.policy.riskSubtree;

import vahy.RiskState;
import vahy.RiskStateWrapper;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.ralph.metadata.RalphMetadata;
import vahy.utils.ImmutableTuple;

import java.util.ArrayDeque;

public class SubtreePriorRiskCalculator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>>
    implements SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState> {

    @Override
    public double calculateRisk(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> subtreeRoot) {
        double totalRisk = 0;
        var inGameEntityId = subtreeRoot.getStateWrapper().getInGameEntityId();
        var queue = new ArrayDeque<ImmutableTuple<SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>, Double>>();
        queue.add(new ImmutableTuple<>(subtreeRoot, 1.0));
        while(!queue.isEmpty()) {
            var node = queue.poll();
            if(node.getFirst().isLeaf()) {
                if(node.getFirst().isFinalNode()) {
                    totalRisk += ((RiskStateWrapper<TAction, TObservation, TState>)node.getFirst().getStateWrapper()).isRiskHit() ? node.getSecond() : 0.0;
                } else {
                    totalRisk += node.getSecond() * node.getFirst().getSearchNodeMetadata().getExpectedRisk()[inGameEntityId];
                }
            } else {
                for (var entry : node.getFirst().getChildNodeMap().entrySet()) {
                    queue.addLast(new ImmutableTuple<>(entry.getValue(), entry.getValue().getSearchNodeMetadata().getPriorProbability() * node.getSecond()));
                }
            }
        }
        return totalRisk;
    }

    @Override
    public String toLog() {
        return "SUBTREE_RISK_PRIOR_SUM";
    }
}
