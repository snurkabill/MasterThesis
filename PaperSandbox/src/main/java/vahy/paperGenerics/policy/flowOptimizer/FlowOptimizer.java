package vahy.paperGenerics.policy.flowOptimizer;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.utils.ImmutableTuple;

import java.util.SplittableRandom;

public interface FlowOptimizer<
    TAction extends Action,
    TReward extends DoubleReward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> {

    ImmutableTuple<Double, Boolean> optimizeFlow(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node,
                                                 SplittableRandom random,
                                                 double totalRiskAllowed);

}
