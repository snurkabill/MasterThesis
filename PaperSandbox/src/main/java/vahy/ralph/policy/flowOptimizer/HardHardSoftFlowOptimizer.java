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

public class HardHardSoftFlowOptimizer<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>>
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
