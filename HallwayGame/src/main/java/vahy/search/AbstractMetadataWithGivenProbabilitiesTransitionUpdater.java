package vahy.search;


//public class AbstractMetadataWithGivenProbabilitiesTransitionUpdater extends MaximizingRewardGivenProbabilities implements NodeTransitionUpdater<
//    ActionType,
//    DoubleReward,
//    DoubleVector,
//    BaseSearchNodeMetadata<DoubleReward>,
//    ImmutableStateImpl> {
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
//        SearchNode<ActionType, DoubleReward, DoubleVector, BaseSearchNodeMetadata<DoubleReward>, State<ActionType, DoubleReward, DoubleVector>> parent,
//        SearchNode<
//            ActionType,
//            DoubleReward,
//            DoubleVector,
//            AbstractStateActionMetadata<DoubleReward>,
//            BaseSearchNodeMetadata<ActionType, DoubleReward, AbstractStateActionMetadata<DoubleReward>>,
//            State<ActionType, DoubleReward, DoubleVector>> child,
//        ActionType action) {
//        BaseSearchNodeMetadata<ActionType, DoubleReward, AbstractStateActionMetadata<DoubleReward>> parentSearchNodeMetadata = parent.getSearchNodeMetadata();
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
//    public void applyUpdate(SearchNode<ActionType, DoubleReward, DoubleVector, BaseSearchNodeMetadata<DoubleReward>, ImmutableStateImpl> evaluatedNode,
//                            SearchNode<ActionType, DoubleReward, DoubleVector, BaseSearchNodeMetadata<DoubleReward>, ImmutableStateImpl> parent,
//                            SearchNode<ActionType, DoubleReward, DoubleVector, BaseSearchNodeMetadata<DoubleReward>, ImmutableStateImpl> child) {
//
//        BaseSearchNodeMetadata<DoubleReward> parentSearchNodeMetadata = parent.getSearchNodeMetadata();
//        Map<ActionType, BaseSearchNodeMetadata<DoubleReward>> childSearchNodeMetadataMap = parent.getChildNodeMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, x-> x.getValue().getSearchNodeMetadata()));
//
//
//
//    }
//}
