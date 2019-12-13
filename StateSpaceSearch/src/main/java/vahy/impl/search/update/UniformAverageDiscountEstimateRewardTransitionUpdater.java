package vahy.impl.search.update;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;

public class UniformAverageDiscountEstimateRewardTransitionUpdater<
    TAction extends Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements NodeTransitionUpdater<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private final double discountFactor;

    public UniformAverageDiscountEstimateRewardTransitionUpdater(double discountFactor) {
        this.discountFactor = discountFactor;
    }

    @Override
    public void applyUpdate(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> evaluatedNode,
                            SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> parent,
                            SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> child) {
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
