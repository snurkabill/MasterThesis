package vahy.ralph.policy.riskSubtree;

import vahy.RiskState;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.ralph.metadata.RalphMetadata;

public class SubtreeRootRiskCalculator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>>
    implements SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState> {

    @Override
    public double calculateRisk(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> subtreeRoot) {
        return subtreeRoot.getSearchNodeMetadata().getExpectedRisk()[subtreeRoot.getStateWrapper().getInGameEntityId()];
    }

    @Override
    public String toLog() {
        return "SUBTREE_RISK_ROOT";
    }
}
