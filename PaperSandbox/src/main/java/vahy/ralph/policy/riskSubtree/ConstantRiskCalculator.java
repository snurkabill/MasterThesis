package vahy.ralph.policy.riskSubtree;

import vahy.RiskState;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.ralph.metadata.RalphMetadata;

public class ConstantRiskCalculator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>>
    implements SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState> {

    private final double riskConstant;

    public ConstantRiskCalculator(double riskConstant) {
        if(riskConstant < 0.0 || riskConstant > 1.0) {
            throw new IllegalArgumentException("Risk must be from interval [0, 1]. Actual value: " + riskConstant);
        }
        this.riskConstant = riskConstant;
    }

    @Override
    public double calculateRisk(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> subtreeRoot) {
        return riskConstant;
    }

    @Override
    public String toLog() {
        return "SUBTREE_RISK_CONSTANT_" + riskConstant;
    }
}
