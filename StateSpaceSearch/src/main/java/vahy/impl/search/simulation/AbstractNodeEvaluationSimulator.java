package vahy.impl.search.simulation;

//public abstract class AbstractNodeEvaluationSimulator<
//    TAction extends Action,
//    TReward extends Reward,
//    TObservation extends Observation,
//    TStateActionMetadata extends StateActionMetadata<TReward>,
//    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
//    TState extends State<TAction, TReward, TObservation>> implements NodeEvaluationSimulator<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {
//
//    private static final Logger logger = LoggerFactory.getLogger(AbstractNodeEvaluationSimulator.class);
//    private final SimpleTimer timer = new SimpleTimer();
//
//    protected final RewardAggregator<TReward> rewardAggregator;
//    protected final double discountFactor;
//
//    protected AbstractNodeEvaluationSimulator(RewardAggregator<TReward> rewardAggregator, double discountFactor) {
//        this.rewardAggregator = rewardAggregator;
//        this.discountFactor = discountFactor;
//    }
//
//    @Override
//    public void calculateMetadataEstimation(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> expandedNode) {
//        if(expandedNode.isFinalNode()) {
//            throw new IllegalStateException("Final node cannot be evaluated anymore");
//        }
//        TSearchNodeMetadata searchNodeMetadata = expandedNode.getSearchNodeMetadata();
//        for (Map.Entry<TAction, SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState>> entry : expandedNode.getChildNodeMap().entrySet()) {
//            timer.startTimer();
//            StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> stateRewardReturn = expandedNode.getWrappedState().applyAction(entry.getKey());
//            TReward expectedReward = calcExpectedReward(entry.getValue());
//            entry.getValue().getSearchNodeMetadata().setExpectedReward(expectedReward);
//            searchNodeMetadata.getStateActionMetadataMap().get(entry.getKey()).setExpectedReward(rewardAggregator.aggregateDiscount(stateRewardReturn.getReward(), expectedReward, discountFactor));
//            timer.stopTimer();
//            logger.debug("Expected reward simulation for action [{}] calculated in [{}] seconds", entry.getKey(), timer.secondsSpent());
//        }
//        expandedNode.getSearchNodeMetadata().setExpectedReward(
//            rewardAggregator.averageReward(searchNodeMetadata
//                .getStateActionMetadataMap()
//                .values()
//                .stream()
//                .map(StateActionMetadata::getExpectedReward)
//            )
//        );
//    }
//
//    protected abstract TReward calcExpectedReward(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> node);
//
//
//}
