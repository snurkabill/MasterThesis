package vahy.impl.search.update;

//public class ArgmaxDiscountEstimatedRewardTransitionUpdater<
//    TAction extends Action,
//    TReward extends Reward,
//    TObservation extends Observation,
//    TSearchNodeMetadata extends SearchNodeMetadata<TReward>,
//    TState extends State<TAction, TReward, TObservation>>
//    implements NodeTransitionUpdater<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {
//
//    private final double discountFactor;
//    private final RewardAggregator<TReward> rewardAggregator;
//
//    public ArgmaxDiscountEstimatedRewardTransitionUpdater(double discountFactor, RewardAggregator<TReward> rewardAggregator) {
//        this.discountFactor = discountFactor;
//        this.rewardAggregator = rewardAggregator;
//    }
//
//    @Override
//    public void applyUpdate(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> evaluatedNode,
//                            SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> parent,
//                            SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> child) {
//
//        if(parent.isPlayerTurn()) {
//            parent.getSearchNodeMetadata().setEstimatedTotalReward(
//                parent
//                    .getChildNodeStream()
//                    .map(x -> x.getSearchNodeMetadata().getEstimatedTotalReward())
//                    .max(Comparable::compareTo)
//                    .orElseThrow(() -> new IllegalStateException("Children should be always expanded when doing transition update")));
//        } else {
//            parent.getSearchNodeMetadata().setEstimatedTotalReward(
//                parent
//                    .getChildNodeStream()
//                    .map(x -> x.getSearchNodeMetadata().getEstimatedTotalReward())
//                    .min(Comparable::compareTo)
//                    .orElseThrow(() -> new IllegalStateException("Children should be always expanded when doing transition update")));
//        }
//
//
//
//    }
//
//    @Override
//    public void applyUpdate(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> parent,
//                            SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> child,
//                            TAction action) {
//        TSearchNodeMetadata parentSearchNodeMetadata = parent.getSearchNodeMetadata();
//
//
//
//        parentSearchNodeMetadata.getStateActionMetadataMap().get(action).setEstimatedTotalReward(
//            rewardAggregator.aggregateDiscount(parentSearchNodeMetadata.getStateActionMetadataMap().get(action).getGainedReward(),
//                child.getSearchNodeMetadata().getEstimatedTotalReward(),
//                discountFactor)
//        );
//        parentSearchNodeMetadata.setEstimatedTotalReward(
//            parentSearchNodeMetadata
//                .getStateActionMetadataMap()
//                .values()
//                .stream()
//                .map(StateActionMetadata::getEstimatedTotalReward)
//                .max(Comparable::compareTo)
//                .orElseThrow(() -> new IllegalStateException("Children should be always expanded when doing transition update")));
//    }
//}
