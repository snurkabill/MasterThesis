package vahy.ralph.policy.flowOptimizer;

import vahy.RiskState;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.ralph.metadata.RalphMetadata;
import vahy.ralph.policy.linearProgram.NoiseStrategy;
import vahy.ralph.policy.linearProgram.OptimalFlowSoftConstraintCalculator;
import vahy.utils.ImmutableTuple;

import java.util.SplittableRandom;

public class SoftFlowOptimizer<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>>
    extends AbstractFlowOptimizer<TAction, TObservation, TSearchNodeMetadata , TState> {

    public SoftFlowOptimizer(SplittableRandom random, NoiseStrategy noiseStrategy) {
        super(random, noiseStrategy);
    }

    @Override
    public ImmutableTuple<Double, Boolean> optimizeFlow(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node, double totalRiskAllowed) {
        var optimalSoftFlowCalculator = new OptimalFlowSoftConstraintCalculator<TAction, TObservation, TSearchNodeMetadata, TState>(totalRiskAllowed, random, noiseStrategy);
        boolean optimalSoftSolutionExists = optimalSoftFlowCalculator.optimizeFlow(node);
        return new ImmutableTuple<>(totalRiskAllowed, optimalSoftSolutionExists);
    }
}
