package vahy.ralph.policy.flowOptimizer;

import vahy.RiskState;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.ralph.metadata.RalphMetadata;
import vahy.ralph.policy.linearProgram.NoiseStrategy;
import vahy.ralph.policy.linearProgram.OptimalFlowHardConstraintCalculator;
import vahy.ralph.policy.riskSubtree.MinimalRiskReachAbilityCalculator;
import vahy.utils.ImmutableTuple;

import java.util.SplittableRandom;

public class HardHardFlowOptimizer<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>>
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
