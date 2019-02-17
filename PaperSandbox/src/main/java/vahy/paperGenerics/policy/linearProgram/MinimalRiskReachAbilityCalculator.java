package vahy.paperGenerics.policy.linearProgram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.riskSubtree.SubtreeRiskCalculator;

import java.util.SplittableRandom;

public class MinimalRiskReachAbilityCalculator<
    TAction extends Action,
    TReward extends DoubleReward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractLinearProgramOnTree<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>
    implements SubtreeRiskCalculator<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {


    private static final Logger logger = LoggerFactory.getLogger(MinimalRiskReachAbilityCalculator.class.getName());

    public MinimalRiskReachAbilityCalculator(SplittableRandom random) {
        super(random, false);
    }

    @Override
    protected void setLeafObjective(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node) {
        if(node.getWrappedState().isRiskHit()) {
            model.setObjectiveCoefficient(node.getSearchNodeMetadata().getNodeProbabilityFlow(), 1.0);
        } else {
            model.setObjectiveCoefficient(node.getSearchNodeMetadata().getNodeProbabilityFlow(), node.getSearchNodeMetadata().getPredictedRisk());
        }
    }

    @Override
    public double calculateRisk(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> subtreeRoot) {
        var isFeasible = optimizeFlow(subtreeRoot);
        if(!isFeasible) {
            throw new IllegalStateException("Minimal risk reachAbility is not feasible. Should not happen. Investigate.");
        }
        return this.getObjectiveValue();
    }
}
