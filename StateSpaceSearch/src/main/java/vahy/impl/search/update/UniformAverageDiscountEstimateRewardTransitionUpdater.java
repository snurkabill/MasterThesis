package vahy.impl.search.update;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.update.NodeTransitionUpdater;

public class UniformAverageDiscountEstimateRewardTransitionUpdater<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata<TReward>,
    TState extends State<TAction, TReward, TObservation, TState>>
    implements NodeTransitionUpdater<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    private final double discountFactor;
    private final RewardAggregator<TReward> rewardAggregator;

    public UniformAverageDiscountEstimateRewardTransitionUpdater(double discountFactor, RewardAggregator<TReward> rewardAggregator) {
        this.discountFactor = discountFactor;
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public void applyUpdate(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> evaluatedNode,
                            SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> parent,
                            SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> child) {
        parent.getSearchNodeMetadata().setExpectedReward(
            rewardAggregator.averageReward(parent
                .getChildNodeStream()
                .map(x -> rewardAggregator.aggregateDiscount(x.getSearchNodeMetadata().getGainedReward(), x.getSearchNodeMetadata().getExpectedReward(), discountFactor))
            )
        );
    }
}
