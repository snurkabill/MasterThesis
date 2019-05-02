package vahy.paperGenerics.policy.flowOptimizer;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.linearProgram.OptimalFlowHardConstraintCalculator;
import vahy.paperGenerics.policy.linearProgram.OptimalFlowSoftConstraint;
import vahy.utils.ImmutableTuple;

import java.util.SplittableRandom;

public class HardSoftFlowOptimizer<
    TAction extends Action,
    TReward extends DoubleReward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    implements FlowOptimizer<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata , TState> {

    @Override
    public ImmutableTuple<Double, Boolean> optimizeFlow(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node,
                                                        SplittableRandom random,
                                                        double totalRiskAllowed) {
        var optimalFlowCalculator =
            new OptimalFlowHardConstraintCalculator<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>(random, totalRiskAllowed);
        boolean optimalSolutionExists = optimalFlowCalculator.optimizeFlow(node);
        if(!optimalSolutionExists) {
            var optimalSoftFlowCalculator =
                new OptimalFlowSoftConstraint<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>(random, totalRiskAllowed);
            optimalSolutionExists = optimalSoftFlowCalculator.optimizeFlow(node);
        }
        return new ImmutableTuple<>(totalRiskAllowed, optimalSolutionExists);
    }
}
