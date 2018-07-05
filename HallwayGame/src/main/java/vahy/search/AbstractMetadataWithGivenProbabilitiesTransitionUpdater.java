package vahy.search;


import vahy.api.model.State;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.ActionType;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarRewardDouble;
import vahy.impl.search.node.nodeMetadata.AbstractSearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.AbstractStateActionMetadata;

public class AbstractMetadataWithGivenProbabilitiesTransitionUpdater extends MaximizingRewardGivenProbabilities implements NodeTransitionUpdater<
    ActionType,
    DoubleScalarRewardDouble,
    DoubleVectorialObservation,
    AbstractStateActionMetadata<DoubleScalarRewardDouble>,
    AbstractSearchNodeMetadata<ActionType, DoubleScalarRewardDouble, AbstractStateActionMetadata<DoubleScalarRewardDouble>>,
    State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> {

    private final double discountFactor;
    private final RewardAggregator<DoubleScalarRewardDouble> rewardAggregator;

    public AbstractMetadataWithGivenProbabilitiesTransitionUpdater(double discountFactor, RewardAggregator<DoubleScalarRewardDouble> rewardAggregator) {
        this.discountFactor = discountFactor;
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public void applyUpdate(
        SearchNode<
                    ActionType,
            DoubleScalarRewardDouble,
                    DoubleVectorialObservation,
                    AbstractStateActionMetadata<DoubleScalarRewardDouble>,
                    AbstractSearchNodeMetadata<ActionType, DoubleScalarRewardDouble, AbstractStateActionMetadata<DoubleScalarRewardDouble>>,
                    State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> parent,
        SearchNode<
            ActionType,
            DoubleScalarRewardDouble,
            DoubleVectorialObservation,
            AbstractStateActionMetadata<DoubleScalarRewardDouble>,
            AbstractSearchNodeMetadata<ActionType, DoubleScalarRewardDouble, AbstractStateActionMetadata<DoubleScalarRewardDouble>>,
            State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> child,
        ActionType action) {
        AbstractSearchNodeMetadata<ActionType, DoubleScalarRewardDouble, AbstractStateActionMetadata<DoubleScalarRewardDouble>> parentSearchNodeMetadata = parent.getSearchNodeMetadata();
        AbstractStateActionMetadata<DoubleScalarRewardDouble> stateActionMetadata = parentSearchNodeMetadata.getStateActionMetadataMap().get(action);

        stateActionMetadata.setEstimatedTotalReward(new DoubleScalarRewardDouble(rewardAggregator.aggregateDiscount(
                stateActionMetadata.getGainedReward(),
                child.getSearchNodeMetadata().getEstimatedTotalReward(),
            discountFactor).getValue()));
//        double parentCumulativeEstimates = parentSearchNodeMetadata.getEstimatedTotalReward().getValue();

        DoubleScalarRewardDouble newParentCumulativeEstimate = resolveReward(parent.getWrappedState(), parent.getSearchNodeMetadata().getStateActionMetadataMap());

//        double sum = parentCumulativeEstimates + newParentCumulativeEstimate.getValue();
        parentSearchNodeMetadata.setEstimatedTotalReward(new DoubleScalarRewardDouble(newParentCumulativeEstimate.getValue()));
    }


}
