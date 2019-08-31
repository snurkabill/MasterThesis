package vahy.paperGenerics.policy.flowOptimizer;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.linearProgram.OptimalFlowSoftConstraint;
import vahy.utils.ImmutableTuple;

import java.util.SplittableRandom;

public class HardHardSoftFlowOptimizer<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements FlowOptimizer<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata , TState> {

    @Override
    public ImmutableTuple<Double, Boolean> optimizeFlow(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node, SplittableRandom random, double totalRiskAllowed) {

        var hardHardFlowOptimizer = new HardHardFlowOptimizer<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>();
        var result = hardHardFlowOptimizer.optimizeFlow(node, random, totalRiskAllowed);

        if(!result.getSecond()) {
            var optimalSoftFlowCalculator = new OptimalFlowSoftConstraint<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>(random, totalRiskAllowed);
            boolean optimalSolutionExists = optimalSoftFlowCalculator.optimizeFlow(node);
            return new ImmutableTuple<>(result.getFirst(), optimalSolutionExists);
        } else {
            return result;
        }
    }
}
