package vahy.impl.search.simulation;

//public class MonteCarloSimulator<
//    TAction extends Action,
//    TReward extends Reward,
//    TObservation extends Observation,
//    TSearchNodeMetadata extends StateValueMetadataImpl<TAction, TReward, TReward>,
//    TState extends State<TAction, TReward, TObservation>>
//    implements NodeEvaluationSimulator<TAction, TReward, TObservation, TSearchNodeMetadata, State<TAction, TReward, TObservation>> {
//
//    private final int simulationCount;
//    private final SplittableRandom random;
//    private final RewardAggregator<TReward> rewardAggregator;
//
//    public MonteCarloSimulator(int simulationCount, SplittableRandom random, RewardAggregator<TReward> rewardAggregator) {
//        this.simulationCount = simulationCount;
//        this.random = random;
//        this.rewardAggregator = rewardAggregator;
//    }
//
//    @Override
//    public void calculateMetadataEstimation(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, State<TAction, TReward, TObservation>> expandedNode) {
//        TSearchNodeMetadata searchNodeMetadata = expandedNode.getSearchNodeMetadata();
////        expandedNode
////            .getChildNodeMap()
////            .entrySet()
////            .stream()
////            .map(entry -> calcExpectedReward(entry.getValue()))
////            .max(Comparable::compareTo)
////            .ifPresent(tReward -> searchNodeMetadata.setCumulativeReward());
////
//
//        for (Map.Entry<TAction, SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, State<TAction, TReward, TObservation>>> entry : expandedNode.getChildNodeMap().entrySet()) {
//            TReward expectedReward = calcExpectedReward(entry.getValue());
//
//            searchNodeMetadata.getStateActionMetadataMap().get(entry.getKey()).getGainedReward();
//
//        }
//    }
//
//    private TReward calcExpectedReward(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, State<TAction, TReward, TObservation>> node) {
//        List<TReward> aggregatedRewardsList = new ArrayList<>();
//
//        for (int i = 0; i < simulationCount; i++) {
//            aggregatedRewardsList.add(runRandomWalkSimulation(node));
//        }
//        return rewardAggregator.expectedReward(aggregatedRewardsList);
//    }
//
//    private TReward runRandomWalkSimulation(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, State<TAction, TReward, TObservation>> node) {
//        State<TAction, TReward, TObservation> wrappedState = node.getWrappedState();
//        List<TReward> gainedRewards = new ArrayList<>();
//
//        while(!wrappedState.isFinalState()) {
//            TAction[] actions = wrappedState.getAllPossibleActions();
//            int actionIndex = random.nextInt(actions.length);
//            StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> stateRewardReturn = wrappedState.applyAction(actions[actionIndex]);
//            wrappedState = stateRewardReturn.getState();
//            gainedRewards.add(stateRewardReturn.getReward());
//        }
//        return rewardAggregator.aggregate(gainedRewards);
//    }
//
//    private StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> doNextStep(State<TAction, TReward, TObservation> wrappedState) {
//
//    }
//}
