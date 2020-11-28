package vahy.paperGenerics.policy.flowOptimizer;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.linearProgram.NoiseStrategy;
import vahy.paperGenerics.policy.linearProgram.OptimalFlowSoftConstraintCalculator;
import vahy.utils.ImmutableTuple;

import java.util.SplittableRandom;

public class SoftFlowOptimizer<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
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
