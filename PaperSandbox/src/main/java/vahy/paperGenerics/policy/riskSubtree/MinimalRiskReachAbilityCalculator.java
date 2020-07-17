package vahy.paperGenerics.policy.riskSubtree;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.PaperStateWrapper;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.linearProgram.AbstractLinearProgramOnTreeWithFixedOpponents;
import vahy.paperGenerics.policy.linearProgram.NoiseStrategy;

public class MinimalRiskReachAbilityCalculator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    implements SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState> {

    @Override
    public double calculateRisk(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> subtreeRoot) {
        if(subtreeRoot.isLeaf()) {
            return ((PaperStateWrapper<TAction, TObservation, TState>)subtreeRoot.getStateWrapper()).isRiskHit() ?  1.0 : 0.0;
        }

        var linProgram = new AbstractLinearProgramOnTreeWithFixedOpponents<TAction, TObservation, TSearchNodeMetadata, TState>(false, null, NoiseStrategy.NONE) {

            @Override
            protected void setLeafObjective(InnerElement element) {
                var node = element.getNode();
                var inGameEntityId = node.getStateWrapper().getInGameEntityId();
                if(((PaperStateWrapper<TAction, TObservation, TState>)node.getStateWrapper()).isRiskHit()) {
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
        };

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
