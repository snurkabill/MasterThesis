package vahy.search;


//public class AbstractMetadataWithGivenProbabilitiesTransitionUpdater extends MaximizingRewardGivenProbabilities implements NodeTransitionUpdater<
//    HallwayAction,
//    DoubleReward,
//    DoubleVector,
//    BaseSearchNodeMetadata<DoubleReward>,
//    HallwayStateImpl> {
//
//    private final double discountFactor;
//    private final RewardAggregator<DoubleReward> rewardAggregator;
//
//    public AbstractMetadataWithGivenProbabilitiesTransitionUpdater(double discountFactor, RewardAggregator<DoubleReward> rewardAggregator) {
//        this.discountFactor = discountFactor;
//        this.rewardAggregator = rewardAggregator;
//    }
//
//    @Override
//    public void applyUpdate(
//        SearchNode<HallwayAction, DoubleReward, DoubleVector, BaseSearchNodeMetadata<DoubleReward>, State<HallwayAction, DoubleReward, DoubleVector>> parent,
//        SearchNode<
//            HallwayAction,
//            DoubleReward,
//            DoubleVector,
//            AbstractStateActionMetadata<DoubleReward>,
//            BaseSearchNodeMetadata<HallwayAction, DoubleReward, AbstractStateActionMetadata<DoubleReward>>,
//            State<HallwayAction, DoubleReward, DoubleVector>> child,
//        HallwayAction action) {
//        BaseSearchNodeMetadata<HallwayAction, DoubleReward, AbstractStateActionMetadata<DoubleReward>> parentSearchNodeMetadata = parent.getSearchNodeMetadata();
//        AbstractStateActionMetadata<DoubleReward> stateActionMetadata = parentSearchNodeMetadata.getStateActionMetadataMap().get(action);
//
//        stateActionMetadata.setExpectedReward(new DoubleReward(rewardAggregator.aggregateDiscount(
//                stateActionMetadata.getGainedReward(),
//                child.getSearchNodeMetadata().getExpectedReward(),
//            discountFactor).getValue()));
////        double parentCumulativeEstimates = parentSearchNodeMetadata.getExpectedReward().getValue();
//
//        DoubleReward newParentCumulativeEstimate = resolveReward(parent.getWrappedState(), parent.getSearchNodeMetadata().getStateActionMetadataMap());
//
////        double sum = parentCumulativeEstimates + newParentCumulativeEstimate.getValue();
//        parentSearchNodeMetadata.setExpectedReward(new DoubleReward(newParentCumulativeEstimate.getValue()));
//    }
//
//
//
//    @Override
//    public void applyUpdate(SearchNode<HallwayAction, DoubleReward, DoubleVector, BaseSearchNodeMetadata<DoubleReward>, HallwayStateImpl> evaluatedNode,
//                            SearchNode<HallwayAction, DoubleReward, DoubleVector, BaseSearchNodeMetadata<DoubleReward>, HallwayStateImpl> parent,
//                            SearchNode<HallwayAction, DoubleReward, DoubleVector, BaseSearchNodeMetadata<DoubleReward>, HallwayStateImpl> child) {
//
//        BaseSearchNodeMetadata<DoubleReward> parentSearchNodeMetadata = parent.getSearchNodeMetadata();
//        Map<HallwayAction, BaseSearchNodeMetadata<DoubleReward>> childSearchNodeMetadataMap = parent.getChildNodeMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, x-> x.getValue().getSearchNodeMetadata()));
//
//
//
//    }
//}
