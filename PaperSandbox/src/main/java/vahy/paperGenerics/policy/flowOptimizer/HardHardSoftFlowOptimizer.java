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

public class HardHardSoftFlowOptimizer<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    extends AbstractFlowOptimizer<TAction, TObservation, TSearchNodeMetadata , TState> {

    private final HardHardFlowOptimizer<TAction, TObservation, TSearchNodeMetadata, TState> hardHardFlowOptimizer;

    public HardHardSoftFlowOptimizer(SplittableRandom random, NoiseStrategy noiseStrategy) {
        super( random, noiseStrategy);
        this.hardHardFlowOptimizer = new HardHardFlowOptimizer<>(random, noiseStrategy);
    }

    @Override
    public ImmutableTuple<Double, Boolean> optimizeFlow(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node, double totalRiskAllowed) {
        var result = hardHardFlowOptimizer.optimizeFlow(node, totalRiskAllowed);

        if(!result.getSecond()) {
            var optimalSoftFlowCalculator = new OptimalFlowSoftConstraintCalculator<TAction, TObservation, TSearchNodeMetadata, TState>(totalRiskAllowed, random, noiseStrategy);
            boolean optimalSolutionExists = optimalSoftFlowCalculator.optimizeFlow(node);
            return new ImmutableTuple<>(result.getFirst(), optimalSolutionExists);
        } else {
            return result;
        }
    }
}
