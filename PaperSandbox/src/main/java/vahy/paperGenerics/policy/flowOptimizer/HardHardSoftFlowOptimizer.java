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
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractFlowOptimizer<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata , TState> {

    private final HardHardFlowOptimizer<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> hardHardFlowOptimizer;

    public HardHardSoftFlowOptimizer(SplittableRandom random, NoiseStrategy noiseStrategy) {
        super( random, noiseStrategy);
        this.hardHardFlowOptimizer = new HardHardFlowOptimizer<>(random, noiseStrategy);
    }

    @Override
    public ImmutableTuple<Double, Boolean> optimizeFlow(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node, double totalRiskAllowed) {
        var result = hardHardFlowOptimizer.optimizeFlow(node, totalRiskAllowed);

        if(!result.getSecond()) {
            var optimalSoftFlowCalculator = new OptimalFlowSoftConstraintCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>(totalRiskAllowed, random, noiseStrategy);
            boolean optimalSolutionExists = optimalSoftFlowCalculator.optimizeFlow(node);
            return new ImmutableTuple<>(result.getFirst(), optimalSolutionExists);
        } else {
            return result;
        }
    }
}
