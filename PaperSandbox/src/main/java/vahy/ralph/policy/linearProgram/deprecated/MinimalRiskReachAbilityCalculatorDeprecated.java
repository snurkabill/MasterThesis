package vahy.ralph.policy.linearProgram.deprecated;

import vahy.RiskState;
import vahy.RiskStateWrapper;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.ralph.metadata.RalphMetadata;
import vahy.ralph.policy.linearProgram.NoiseStrategy;
import vahy.ralph.policy.riskSubtree.SubtreeRiskCalculator;

import java.util.SplittableRandom;

public class MinimalRiskReachAbilityCalculatorDeprecated<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>>
    implements SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState> {

    private final Class<TAction> actionClass;

    public MinimalRiskReachAbilityCalculatorDeprecated(Class<TAction> actionClass) {
        this.actionClass = actionClass;
    }


    private class InnerRiskCalculator extends AbstractLinearProgramOnTreeDeprecated<TAction, TObservation, TSearchNodeMetadata, TState> {

        public InnerRiskCalculator(Class<TAction> tActionClass, boolean maximize, SplittableRandom random, NoiseStrategy strategy) {
            super(tActionClass, maximize, random, strategy);
        }

        @Override
        protected void setLeafObjective(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node) {
            if(((RiskStateWrapper<TAction, TObservation, TState>)node.getStateWrapper()).isRiskHit()) {
                model.setObjectiveCoefficient(node.getSearchNodeMetadata().getNodeProbabilityFlow(), 1.0);
            } else {
                model.setObjectiveCoefficient(node.getSearchNodeMetadata().getNodeProbabilityFlow(), node.getSearchNodeMetadata().getExpectedRisk()[node.getStateWrapper().getInGameEntityId()]);
            }
        }

        @Override
        protected void finalizeHardConstraints() {
            // this is it
        }
    }

    @Override
    public double calculateRisk(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> subtreeRoot) {

        if(subtreeRoot.isLeaf()) {
            return ((RiskStateWrapper<TAction, TObservation, TState>)subtreeRoot.getStateWrapper()).isRiskHit() ?  1.0 : 0.0;
        }
        var linProgram = new InnerRiskCalculator(actionClass, false, null, NoiseStrategy.NONE);
        var isFeasible = linProgram.optimizeFlow(subtreeRoot);
        if(!isFeasible) {
            throw new IllegalStateException("Minimal risk reachAbility is not feasible. Should not happen. Investigate.");
        }
        return linProgram.getObjectiveValue();
    }

    @Override
    public String toLog() {
        return "SUBTREE_RISK_MINIMAL_RISK_REACHABILITY";
    }
}
