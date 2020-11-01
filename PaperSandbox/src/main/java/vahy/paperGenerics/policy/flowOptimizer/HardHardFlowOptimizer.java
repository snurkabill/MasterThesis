package vahy.paperGenerics.policy.flowOptimizer;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.linearProgram.NoiseStrategy;
import vahy.paperGenerics.policy.linearProgram.OptimalFlowHardConstraintCalculator;
import vahy.paperGenerics.policy.riskSubtree.MinimalRiskReachAbilityCalculator;
import vahy.utils.ImmutableTuple;

import java.util.SplittableRandom;

public class HardHardFlowOptimizer<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    extends AbstractFlowOptimizer<TAction, TObservation, TSearchNodeMetadata , TState> {

    public HardHardFlowOptimizer(SplittableRandom random, NoiseStrategy noiseStrategy) {
        super(random, noiseStrategy);
    }

    @Override
    public ImmutableTuple<Double, Boolean> optimizeFlow(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node, double totalRiskAllowed) {
        var optimalFlowCalculator = new OptimalFlowHardConstraintCalculator<TAction, TObservation, TSearchNodeMetadata, TState>(totalRiskAllowed, random, noiseStrategy);
        boolean optimalSolutionExists = optimalFlowCalculator.optimizeFlow(node);

        if(optimalSolutionExists) {
            return new ImmutableTuple<>(totalRiskAllowed, optimalSolutionExists);
        }

        var minimalRiskReachAbilityCalculator = new MinimalRiskReachAbilityCalculator<TAction, TObservation, TSearchNodeMetadata, TState>();
        totalRiskAllowed = minimalRiskReachAbilityCalculator.calculateRisk(node);

        var optimalFlowCalculatorWithFixedRisk = new OptimalFlowHardConstraintCalculator<TAction, TObservation, TSearchNodeMetadata, TState>(totalRiskAllowed, random, noiseStrategy);
        optimalSolutionExists = optimalFlowCalculatorWithFixedRisk.optimizeFlow(node);
        return new ImmutableTuple<>(totalRiskAllowed, optimalSolutionExists);
    }
}
