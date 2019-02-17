package vahy.paperGenerics.policy.riskSubtree;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.linearProgram.MinimalRiskReachAbilityCalculator;

import java.util.SplittableRandom;

public class SubtreeMinimalRiskCalculator<
    TAction extends Action,
    TReward extends DoubleReward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    implements SubtreeRiskCalculator<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private final SplittableRandom random;

    public SubtreeMinimalRiskCalculator(SplittableRandom random) {
        this.random = random;
    }

    @Override
    public double calculateRisk(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> subtreeRoot) {
        var minimalRiskReachAbilityCalculator = new MinimalRiskReachAbilityCalculator<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>(random);
        var feasible = minimalRiskReachAbilityCalculator.optimizeFlow(subtreeRoot);

        if(feasible) {
            return minimalRiskReachAbilityCalculator.getObjectiveValue();
        } else {
            throw new IllegalStateException("Minimal risk Reachability is not feasible");
        }
    }
}
