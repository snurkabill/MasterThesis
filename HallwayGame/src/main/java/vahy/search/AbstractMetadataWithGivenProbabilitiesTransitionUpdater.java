package vahy.search;


import vahy.api.model.State;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.ActionType;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.node.nodeMetadata.AbstractSearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.AbstractStateActionMetadata;

public class AbstractMetadataWithGivenProbabilitiesTransitionUpdater extends MaximizingRewardGivenProbabilities implements NodeTransitionUpdater<
    ActionType,
    DoubleScalarReward,
    DoubleVectorialObservation,
    AbstractStateActionMetadata<DoubleScalarReward>,
    AbstractSearchNodeMetadata<ActionType, DoubleScalarReward, AbstractStateActionMetadata<DoubleScalarReward>>,
    State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> {

    private final double discountFactor;
    private final RewardAggregator<DoubleScalarReward> rewardAggregator;

    public AbstractMetadataWithGivenProbabilitiesTransitionUpdater(double discountFactor, RewardAggregator<DoubleScalarReward> rewardAggregator) {
        this.discountFactor = discountFactor;
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public void applyUpdate(
        SearchNode<
                    ActionType,
            DoubleScalarReward,
                    DoubleVectorialObservation,
                    AbstractStateActionMetadata<DoubleScalarReward>,
                    AbstractSearchNodeMetadata<ActionType, DoubleScalarReward, AbstractStateActionMetadata<DoubleScalarReward>>,
                    State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> parent,
        SearchNode<
            ActionType,
            DoubleScalarReward,
            DoubleVectorialObservation,
            AbstractStateActionMetadata<DoubleScalarReward>,
            AbstractSearchNodeMetadata<ActionType, DoubleScalarReward, AbstractStateActionMetadata<DoubleScalarReward>>,
            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> child,
        ActionType action) {
        AbstractSearchNodeMetadata<ActionType, DoubleScalarReward, AbstractStateActionMetadata<DoubleScalarReward>> parentSearchNodeMetadata = parent.getSearchNodeMetadata();
        AbstractStateActionMetadata<DoubleScalarReward> stateActionMetadata = parentSearchNodeMetadata.getStateActionMetadataMap().get(action);

        stateActionMetadata.setEstimatedTotalReward(new DoubleScalarReward(rewardAggregator.aggregateDiscount(
                stateActionMetadata.getGainedReward(),
                child.getSearchNodeMetadata().getEstimatedTotalReward(),
            discountFactor).getValue()));
//        double parentCumulativeEstimates = parentSearchNodeMetadata.getEstimatedTotalReward().getValue();

        DoubleScalarReward newParentCumulativeEstimate = resolveReward(parent.getWrappedState(), parent.getSearchNodeMetadata().getStateActionMetadataMap());

//        double sum = parentCumulativeEstimates + newParentCumulativeEstimate.getValue();
        parentSearchNodeMetadata.setEstimatedTotalReward(new DoubleScalarReward(newParentCumulativeEstimate.getValue()));
    }


}
