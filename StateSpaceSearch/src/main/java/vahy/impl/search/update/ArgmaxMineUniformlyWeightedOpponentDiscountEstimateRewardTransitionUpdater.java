package vahy.impl.search.update;

//public class ArgmaxMineUniformlyWeightedOpponentDiscountEstimateRewardTransitionUpdater<
//    TAction extends Action,
//    TReward extends Reward,
//    TObservation extends Observation,
//    TStateActionMetadata extends StateActionMetadata<TReward>,
//    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
//    TState extends State<TAction, TReward, TObservation>>
//    implements NodeTransitionUpdater<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {
//
//    private final double discountFactor;
//    private final RewardAggregator<TReward> rewardAggregator;
//
//    public ArgmaxMineUniformlyWeightedOpponentDiscountEstimateRewardTransitionUpdater(double discountFactor, RewardAggregator<TReward> rewardAggregator) {
//        this.discountFactor = discountFactor;
//        this.rewardAggregator = rewardAggregator;
//    }
//
//    @Override
//    public void applyUpdate(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> parent,
//                            SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> child,
//                            TAction action) {
//        TSearchNodeMetadata parentSearchNodeMetadata = parent.getSearchNodeMetadata();
//        parentSearchNodeMetadata.getStateActionMetadataMap().get(action).setEstimatedTotalReward(
//            rewardAggregator.aggregateDiscount(parentSearchNodeMetadata.getStateActionMetadataMap().get(action).getGainedReward(),
//                child.getSearchNodeMetadata().getEstimatedTotalReward(),
//                discountFactor)
//        );
//        parentSearchNodeMetadata.setEstimatedTotalReward(
//            parent.isOpponentTurn() ?
//                rewardAggregator.averageReward(parentSearchNodeMetadata
//                    .getStateActionMetadataMap()
//                    .values()
//                    .stream()
//                    .map(StateActionMetadata::getEstimatedTotalReward)
//                )
//                : parentSearchNodeMetadata
//                    .getStateActionMetadataMap()
//                    .values()
//                    .stream()
//                    .map(StateActionMetadata::getEstimatedTotalReward)
//                    .max(Comparable::compareTo)
//                    .orElseThrow(() -> new IllegalStateException("Children should be always expanded when doing transition update"))
//        );
//    }
//}
