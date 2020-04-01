package vahy.paperGenerics.policy.riskSubtree;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;

public class ConstantRiskCalculator<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private final double riskConstant;

    public ConstantRiskCalculator(double riskConstant) {
        if(riskConstant < 0.0 || riskConstant > 1.0) {
            throw new IllegalArgumentException("Risk must be from interval [0, 1]. Actual value: " + riskConstant);
        }
        this.riskConstant = riskConstant;
    }

    @Override
    public double calculateRisk(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> subtreeRoot) {
        return riskConstant;
    }

    @Override
    public String toLog() {
        return "SUBTREE_RISK_CONSTANT_" + riskConstant;
    }
}
