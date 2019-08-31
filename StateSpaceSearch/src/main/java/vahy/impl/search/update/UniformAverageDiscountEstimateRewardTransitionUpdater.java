package vahy.impl.search.update;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.update.NodeTransitionUpdater;

public class UniformAverageDiscountEstimateRewardTransitionUpdater<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements NodeTransitionUpdater<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private final double discountFactor;
    private final RewardAggregator rewardAggregator;

    public UniformAverageDiscountEstimateRewardTransitionUpdater(double discountFactor, RewardAggregator rewardAggregator) {
        this.discountFactor = discountFactor;
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public void applyUpdate(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> evaluatedNode,
                            SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> parent,
                            SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> child) {
        parent.getSearchNodeMetadata().setExpectedReward(
            rewardAggregator.averageReward(parent
                .getChildNodeStream()
                .map(x -> rewardAggregator.aggregateDiscount(x.getSearchNodeMetadata().getGainedReward(), x.getSearchNodeMetadata().getExpectedReward(), discountFactor))
            )
        );
    }
}
