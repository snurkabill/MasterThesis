package vahy.paperGenerics.policy.riskSubtree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.linearProgram.AbstractLinearProgramOnTree;

import java.util.SplittableRandom;

public class MinimalRiskReachAbilityCalculator<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(MinimalRiskReachAbilityCalculator.class.getName());

    private final SplittableRandom random;

    public MinimalRiskReachAbilityCalculator(SplittableRandom random) {
        this.random = random;
    }

    @Override
    public double calculateRisk(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> subtreeRoot) {

        if(subtreeRoot.isLeaf()) {
            return subtreeRoot.getWrappedState().isRiskHit() ?  1.0 : 0.0;
        }

        var linProgram = new AbstractLinearProgramOnTree<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>(random, false) {
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
                // this is is
            }
        };
        var isFeasible = linProgram.optimizeFlow(subtreeRoot);
        if(!isFeasible) {
            throw new IllegalStateException("Minimal risk reachAbility is not feasible. Should not happen. Investigate.");
        }
        return linProgram.getObjectiveValue();
    }
}
