package vahy.paperGenerics.policy.linearProgram.deprecated;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.linearProgram.NoiseStrategy;
import vahy.paperGenerics.policy.riskSubtree.SubtreeRiskCalculator;

@Deprecated
public class MinimalRiskReachAbilityCalculatorDeprecated<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(MinimalRiskReachAbilityCalculatorDeprecated.class.getName());

    private final Class<TAction> actionClass;

    public MinimalRiskReachAbilityCalculatorDeprecated(Class<TAction> actionClass) {
        this.actionClass = actionClass;
    }

    @Override
    public double calculateRisk(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> subtreeRoot) {

        if(subtreeRoot.isLeaf()) {
            return subtreeRoot.getWrappedState().isRiskHit() ?  1.0 : 0.0;
        }

        var linProgram = new AbstractLinearProgramOnTreeDeprecated<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>(actionClass, false, null, NoiseStrategy.NONE) {
            @Override
            protected void setLeafObjective(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node) {
                if(node.getWrappedState().isRiskHit()) {
                    model.setObjectiveCoefficient(node.getSearchNodeMetadata().getNodeProbabilityFlow(), 1.0);
                } else {
                    model.setObjectiveCoefficient(node.getSearchNodeMetadata().getNodeProbabilityFlow(), node.getSearchNodeMetadata().getPredictedRisk());
                }
            }

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