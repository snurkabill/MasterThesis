package vahy.search;

//public class Ucb1WithGivenProbabilitiesTransitionUpdater extends MaximizingRewardGivenProbabilities implements NodeTransitionUpdater<
//    ActionType,
//    DoubleScalarReward,
//    DoubleVectorialObservation,
//    MonteCarloTreeSearchMetadata<DoubleScalarReward>,
//    ImmutableStateImpl> {
//
//    private final double discountFactor;
//    private final RewardAggregator<DoubleScalarReward> rewardAggregator;
//
//    public Ucb1WithGivenProbabilitiesTransitionUpdater(double discountFactor, RewardAggregator<DoubleScalarReward> rewardAggregator) {
//        this.discountFactor = discountFactor;
//        this.rewardAggregator = rewardAggregator;
//    }
//
//    @Override
//    public void applyUpdate(SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, Ucb1StateActionMetadata<DoubleScalarReward>, MonteCarloTreeSearchMetadata<ActionType, DoubleScalarReward>, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> parent, SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, Ucb1StateActionMetadata<DoubleScalarReward>, MonteCarloTreeSearchMetadata<ActionType, DoubleScalarReward>, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> child, ActionType action) {
//        MonteCarloTreeSearchMetadata<DoubleScalarReward> parentSearchNodeMetadata = parent.getSearchNodeMetadata();
//        Ucb1StateActionMetadata<DoubleScalarReward> stateActionMetadata = parentSearchNodeMetadata.getStateActionMetadataMap().get(action);
//
//        stateActionMetadata.setExpectedReward(new DoubleScalarReward(rewardAggregator.aggregateDiscount(
//            stateActionMetadata.getGainedReward(),
//            child.getSearchNodeMetadata().getExpectedReward(),
//            discountFactor).getValue()));
//        double parentCumulativeEstimates = parentSearchNodeMetadata.getExpectedReward().getValue() * (parentSearchNodeMetadata.getVisitCounter() - 1);
//
//        DoubleScalarReward newParentCumulativeEstimate = resolveReward(
//            parent.getWrappedState(),
//            parent.getSearchNodeMetadata()
//                .getStateActionMetadataMap()
//                .entrySet()
//                .stream()
//                .collect(Collectors.toMap(
//                    Map.Entry::getKey,
//                    o -> (AbstractStateActionMetadata<DoubleScalarReward>) o.getValue()
//                ))
//        );
//
//        double sum = parentCumulativeEstimates + newParentCumulativeEstimate.getValue();
//        parentSearchNodeMetadata.setExpectedReward(new DoubleScalarReward(sum / parentSearchNodeMetadata.getVisitCounter()));
//    }
//
//    @Override
//    public void applyUpdate(SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleScalarReward>, ImmutableStateImpl> evaluatedNode,
//                            SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleScalarReward>, ImmutableStateImpl> parent,
//                            SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleScalarReward>, ImmutableStateImpl> child) {
//
//        MonteCarloTreeSearchMetadata<DoubleScalarReward> searchNodeMetadata = parent.getSearchNodeMetadata();
//
//
//    }
//}
