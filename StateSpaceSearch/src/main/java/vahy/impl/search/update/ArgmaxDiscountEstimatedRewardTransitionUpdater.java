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
//            parent.getSearchNodeMetadata().setExpectedReward(
//                parent
//                    .getChildNodeStream()
//                    .map(x -> x.getSearchNodeMetadata().getExpectedReward())
//                    .max(Comparable::compareTo)
//                    .orElseThrow(() -> new IllegalStateException("Children should be always expanded when doing transition update")));
//        } else {
//            parent.getSearchNodeMetadata().setExpectedReward(
//                parent
//                    .getChildNodeStream()
//                    .map(x -> x.getSearchNodeMetadata().getExpectedReward())
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
//        parentSearchNodeMetadata.getStateActionMetadataMap().get(action).setExpectedReward(
//            rewardAggregator.aggregateDiscount(parentSearchNodeMetadata.getStateActionMetadataMap().get(action).getGainedReward(),
//                child.getSearchNodeMetadata().getExpectedReward(),
//                discountFactor)
//        );
//        parentSearchNodeMetadata.setExpectedReward(
//            parentSearchNodeMetadata
//                .getStateActionMetadataMap()
//                .values()
//                .stream()
//                .map(StateActionMetadata::getExpectedReward)
//                .max(Comparable::compareTo)
//                .orElseThrow(() -> new IllegalStateException("Children should be always expanded when doing transition update")));
//    }
//}
