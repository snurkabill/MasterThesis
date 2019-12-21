package vahy.paperGenerics.policy.flowOptimizer;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.linearProgram.OptimalFlowHardConstraintCalculator;
import vahy.paperGenerics.policy.riskSubtree.MinimalRiskReachAbilityCalculator;
import vahy.utils.ImmutableTuple;

public class HardHardFlowOptimizer<
    TAction extends Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements FlowOptimizer<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata , TState> {

    @Override
    public ImmutableTuple<Double, Boolean> optimizeFlow(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node, double totalRiskAllowed) {

        var optimalFlowCalculator = new OptimalFlowHardConstraintCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>(totalRiskAllowed);
        boolean optimalSolutionExists = optimalFlowCalculator.optimizeFlow(node);

        if(!optimalSolutionExists) {
            var minimalRiskReachAbilityCalculator = new MinimalRiskReachAbilityCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>();
            totalRiskAllowed = minimalRiskReachAbilityCalculator.calculateRisk(node);
        }
        var optimalFlowCalculatorWithFixedRisk = new OptimalFlowHardConstraintCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>(totalRiskAllowed);
        optimalSolutionExists = optimalFlowCalculatorWithFixedRisk.optimizeFlow(node);
        return new ImmutableTuple<>(totalRiskAllowed, optimalSolutionExists);
    }
}
