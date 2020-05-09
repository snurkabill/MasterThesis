package vahy.impl.search.update;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;

public class UniformAverageDiscountEstimateRewardTransitionUpdater<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata,
    TState extends State<TAction, TObservation, TState>>
    implements NodeTransitionUpdater<TAction, TObservation, TSearchNodeMetadata, TState> {

    private final double discountFactor;

    public UniformAverageDiscountEstimateRewardTransitionUpdater(double discountFactor) {
        this.discountFactor = discountFactor;
    }

    @Override
    public void applyUpdate(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> evaluatedNode,
                            SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> parent,
                            SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> child) {
        parent
            .getSearchNodeMetadata()
            .setExpectedReward(DoubleScalarRewardAggregator
                .averageReward(parent
                    .getChildNodeStream()
                    .map(x -> DoubleScalarRewardAggregator.aggregateDiscount(
                        x.getSearchNodeMetadata()
                            .getGainedReward(),
                        x.getSearchNodeMetadata()
                            .getExpectedReward(),
                        discountFactor))
            )
        );
    }
}
