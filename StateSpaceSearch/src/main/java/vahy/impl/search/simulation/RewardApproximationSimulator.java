package vahy.impl.search.simulation;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;

import java.util.function.Function;

public class RewardApproximationSimulator<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>>
    extends AbstractNodeEvaluationSimulator<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {

    private final Function<TObservation, TReward> rewardApproximatorFunction;

    protected RewardApproximationSimulator(RewardAggregator<TReward> rewardAggregator, double discountFactor, Function<TObservation, TReward> rewardApproximatorFunction) {
        super(rewardAggregator, discountFactor);
        this.rewardApproximatorFunction = rewardApproximatorFunction;
    }

    @Override
    protected TReward calcExpectedReward(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> node) {
        return rewardApproximatorFunction.apply(node.getWrappedState().getObservation());
    }
}
