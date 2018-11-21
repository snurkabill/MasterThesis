package vahy.impl.search.simulation;

//public class MonteCarloSimulator<
//    TAction extends Action,
//    TReward extends Reward,
//    TObservation extends Observation,
//    TStateActionMetadata extends StateActionMetadata<TReward>,
//    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
//    TState extends State<TAction, TReward, TObservation>>
//    extends AbstractNodeEvaluationSimulator<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {
//
//    private static final Logger logger = LoggerFactory.getLogger(MonteCarloSimulator.class);
//
//    private final int simulationCount;
//    private final SplittableRandom random;
//    private final SimpleTimer timer = new SimpleTimer();
//
//    public MonteCarloSimulator(int simulationCount, double discountFactor, SplittableRandom random, RewardAggregator<TReward> rewardAggregator) {
//        super(rewardAggregator, discountFactor);
//        this.simulationCount = simulationCount;
//        this.random = random;
//    }
//
//    @Override
//    protected TReward calcExpectedReward(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> node) {
//        List<TReward> aggregatedRewardsList = new ArrayList<>();
//        timer.startTimer();
//        for (int i = 0; i < simulationCount; i++) {
//            aggregatedRewardsList.add(runRandomWalkSimulation(node));
//        }
//        timer.stopTimer();
//        logger.debug("Running [{}] MonteCarlo simulations. Simulations per second: [{}]", simulationCount, timer.samplesPerSec(simulationCount));
//        return rewardAggregator.averageReward(aggregatedRewardsList);
//    }
//
//    private TReward runRandomWalkSimulation(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> node) {
//        List<TReward> gainedRewards = new ArrayList<>();
//        State<TAction, TReward, TObservation> wrappedState = node.getWrappedState();
//        while (!wrappedState.isFinalState()) {
//            TAction[] actions = wrappedState.getAllPossibleActions();
//            int actionIndex = random.nextInt(actions.length);
//            StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> stateRewardReturn = wrappedState.applyAction(actions[actionIndex]);
//            wrappedState = stateRewardReturn.getState();
//            gainedRewards.add(stateRewardReturn.getReward());
//        }
//        return rewardAggregator.aggregateDiscount(gainedRewards, discountFactor);
//    }
//}
