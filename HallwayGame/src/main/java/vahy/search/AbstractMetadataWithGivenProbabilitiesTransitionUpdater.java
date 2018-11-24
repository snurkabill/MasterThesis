package vahy.search;


//public class AbstractMetadataWithGivenProbabilitiesTransitionUpdater extends MaximizingRewardGivenProbabilities implements NodeTransitionUpdater<
//    ActionType,
//    DoubleScalarReward,
//    DoubleVectorialObservation,
//    BaseSearchNodeMetadata<DoubleScalarReward>,
//    ImmutableStateImpl> {
//
//    private final double discountFactor;
//    private final RewardAggregator<DoubleScalarReward> rewardAggregator;
//
//    public AbstractMetadataWithGivenProbabilitiesTransitionUpdater(double discountFactor, RewardAggregator<DoubleScalarReward> rewardAggregator) {
//        this.discountFactor = discountFactor;
//        this.rewardAggregator = rewardAggregator;
//    }
//
//    @Override
//    public void applyUpdate(
//        SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, BaseSearchNodeMetadata<DoubleScalarReward>, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> parent,
//        SearchNode<
//            ActionType,
//            DoubleScalarReward,
//            DoubleVectorialObservation,
//            AbstractStateActionMetadata<DoubleScalarReward>,
//            BaseSearchNodeMetadata<ActionType, DoubleScalarReward, AbstractStateActionMetadata<DoubleScalarReward>>,
//            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> child,
//        ActionType action) {
//        BaseSearchNodeMetadata<ActionType, DoubleScalarReward, AbstractStateActionMetadata<DoubleScalarReward>> parentSearchNodeMetadata = parent.getSearchNodeMetadata();
//        AbstractStateActionMetadata<DoubleScalarReward> stateActionMetadata = parentSearchNodeMetadata.getStateActionMetadataMap().get(action);
//
//        stateActionMetadata.setExpectedReward(new DoubleScalarReward(rewardAggregator.aggregateDiscount(
//                stateActionMetadata.getGainedReward(),
//                child.getSearchNodeMetadata().getExpectedReward(),
//            discountFactor).getValue()));
////        double parentCumulativeEstimates = parentSearchNodeMetadata.getExpectedReward().getValue();
//
//        DoubleScalarReward newParentCumulativeEstimate = resolveReward(parent.getWrappedState(), parent.getSearchNodeMetadata().getStateActionMetadataMap());
//
////        double sum = parentCumulativeEstimates + newParentCumulativeEstimate.getValue();
//        parentSearchNodeMetadata.setExpectedReward(new DoubleScalarReward(newParentCumulativeEstimate.getValue()));
//    }
//
//
//
//    @Override
//    public void applyUpdate(SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, BaseSearchNodeMetadata<DoubleScalarReward>, ImmutableStateImpl> evaluatedNode,
//                            SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, BaseSearchNodeMetadata<DoubleScalarReward>, ImmutableStateImpl> parent,
//                            SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, BaseSearchNodeMetadata<DoubleScalarReward>, ImmutableStateImpl> child) {
//
//        BaseSearchNodeMetadata<DoubleScalarReward> parentSearchNodeMetadata = parent.getSearchNodeMetadata();
//        Map<ActionType, BaseSearchNodeMetadata<DoubleScalarReward>> childSearchNodeMetadataMap = parent.getChildNodeMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, x-> x.getValue().getSearchNodeMetadata()));
//
//
//
//    }
//}
