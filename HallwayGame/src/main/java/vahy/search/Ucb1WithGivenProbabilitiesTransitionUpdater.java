package vahy.search;

//public class Ucb1WithGivenProbabilitiesTransitionUpdater extends MaximizingRewardGivenProbabilities implements NodeTransitionUpdater<
//    ActionType,
//    DoubleReward,
//    DoubleVector,
//    MonteCarloTreeSearchMetadata<DoubleReward>,
//    ImmutableStateImpl> {
//
//    private final double discountFactor;
//    private final RewardAggregator<DoubleReward> rewardAggregator;
//
//    public Ucb1WithGivenProbabilitiesTransitionUpdater(double discountFactor, RewardAggregator<DoubleReward> rewardAggregator) {
//        this.discountFactor = discountFactor;
//        this.rewardAggregator = rewardAggregator;
//    }
//
//    @Override
//    public void applyUpdate(SearchNode<ActionType, DoubleReward, DoubleVector, Ucb1StateActionMetadata<DoubleReward>, MonteCarloTreeSearchMetadata<ActionType, DoubleReward>, State<ActionType, DoubleReward, DoubleVector>> parent, SearchNode<ActionType, DoubleReward, DoubleVector, Ucb1StateActionMetadata<DoubleReward>, MonteCarloTreeSearchMetadata<ActionType, DoubleReward>, State<ActionType, DoubleReward, DoubleVector>> child, ActionType action) {
//        MonteCarloTreeSearchMetadata<DoubleReward> parentSearchNodeMetadata = parent.getSearchNodeMetadata();
//        Ucb1StateActionMetadata<DoubleReward> stateActionMetadata = parentSearchNodeMetadata.getStateActionMetadataMap().get(action);
//
//        stateActionMetadata.setExpectedReward(new DoubleReward(rewardAggregator.aggregateDiscount(
//            stateActionMetadata.getGainedReward(),
//            child.getSearchNodeMetadata().getExpectedReward(),
//            discountFactor).getValue()));
//        double parentCumulativeEstimates = parentSearchNodeMetadata.getExpectedReward().getValue() * (parentSearchNodeMetadata.getVisitCounter() - 1);
//
//        DoubleReward newParentCumulativeEstimate = resolveReward(
//            parent.getWrappedState(),
//            parent.getSearchNodeMetadata()
//                .getStateActionMetadataMap()
//                .entrySet()
//                .stream()
//                .collect(Collectors.toMap(
//                    Map.Entry::getKey,
//                    o -> (AbstractStateActionMetadata<DoubleReward>) o.getValue()
//                ))
//        );
//
//        double sum = parentCumulativeEstimates + newParentCumulativeEstimate.getValue();
//        parentSearchNodeMetadata.setExpectedReward(new DoubleReward(sum / parentSearchNodeMetadata.getVisitCounter()));
//    }
//
//    @Override
//    public void applyUpdate(SearchNode<ActionType, DoubleReward, DoubleVector, MonteCarloTreeSearchMetadata<DoubleReward>, ImmutableStateImpl> evaluatedNode,
//                            SearchNode<ActionType, DoubleReward, DoubleVector, MonteCarloTreeSearchMetadata<DoubleReward>, ImmutableStateImpl> parent,
//                            SearchNode<ActionType, DoubleReward, DoubleVector, MonteCarloTreeSearchMetadata<DoubleReward>, ImmutableStateImpl> child) {
//
//        MonteCarloTreeSearchMetadata<DoubleReward> searchNodeMetadata = parent.getSearchNodeMetadata();
//
//
//    }
//}
