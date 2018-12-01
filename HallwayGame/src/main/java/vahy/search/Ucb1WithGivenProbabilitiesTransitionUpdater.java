package vahy.search;

//public class Ucb1WithGivenProbabilitiesTransitionUpdater extends MaximizingRewardGivenProbabilities implements NodeTransitionUpdater<
//    ActionType,
//    DoubleReward,
//    DoubleVectorialObservation,
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
//    public void applyUpdate(SearchNode<ActionType, DoubleReward, DoubleVectorialObservation, Ucb1StateActionMetadata<DoubleReward>, MonteCarloTreeSearchMetadata<ActionType, DoubleReward>, State<ActionType, DoubleReward, DoubleVectorialObservation>> parent, SearchNode<ActionType, DoubleReward, DoubleVectorialObservation, Ucb1StateActionMetadata<DoubleReward>, MonteCarloTreeSearchMetadata<ActionType, DoubleReward>, State<ActionType, DoubleReward, DoubleVectorialObservation>> child, ActionType action) {
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
//    public void applyUpdate(SearchNode<ActionType, DoubleReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleReward>, ImmutableStateImpl> evaluatedNode,
//                            SearchNode<ActionType, DoubleReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleReward>, ImmutableStateImpl> parent,
//                            SearchNode<ActionType, DoubleReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleReward>, ImmutableStateImpl> child) {
//
//        MonteCarloTreeSearchMetadata<DoubleReward> searchNodeMetadata = parent.getSearchNodeMetadata();
//
//
//    }
//}
