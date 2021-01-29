package vahy.ralph.policy.riskSubtree;

import vahy.RiskState;
import vahy.RiskStateWrapper;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.ralph.metadata.RalphMetadata;
import vahy.ralph.policy.linearProgram.AbstractLinearProgramOnTreeWithFixedOpponents;
import vahy.ralph.policy.linearProgram.NoiseStrategy;

import java.util.SplittableRandom;

public class MinimalRiskReachAbilityCalculator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>>
    implements SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState> {

    private class InnerRiskCalculator extends AbstractLinearProgramOnTreeWithFixedOpponents<TAction, TObservation, TSearchNodeMetadata, TState> {

        public InnerRiskCalculator(boolean maximize, SplittableRandom random, NoiseStrategy strategy) {
            super(maximize, random, strategy);
        }

        @Override
        protected void setLeafObjective(InnerElement element) {
            var node = element.getNode();
            var inGameEntityId = node.getStateWrapper().getInGameEntityId();
            if(((RiskStateWrapper<TAction, TObservation, TState>)node.getStateWrapper()).isRiskHit()) {
                element.getFlowWithCoefficient().setCoefficient(element.getFlowWithCoefficient().getCoefficient() + (1.0 * element.getModifier()));
            } else {
                element.getFlowWithCoefficient().setCoefficient(element.getFlowWithCoefficient().getCoefficient() + (node.getSearchNodeMetadata().getExpectedRisk()[inGameEntityId] * element.getModifier()));
            }
        }

//            @Override
//            protected void setLeafObjectiveWithFlow(List<SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> searchNodes, CLPVariable parentFlow) {
//                double sum = 0.0;
//                for (var entry : searchNodes) {
//                    var inGameEntityId = entry.getStateWrapper().getInGameEntityId();
//                    var metadata = entry.getSearchNodeMetadata();
//                    sum += (((PaperStateWrapper<TAction, TObservation, TState>)entry.getStateWrapper()).isRiskHit() ? 1.0 : metadata.getExpectedRisk()[inGameEntityId]) * metadata.getPriorProbability();
//                }
//                model.setObjectiveCoefficient(parentFlow, sum);
//            }

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

        var linProgram = new InnerRiskCalculator(false, null, NoiseStrategy.NONE);
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
