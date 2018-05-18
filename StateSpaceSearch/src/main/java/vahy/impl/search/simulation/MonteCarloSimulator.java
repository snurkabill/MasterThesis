package vahy.impl.search.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.Reward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.timer.SimpleTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public class MonteCarloSimulator<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>>
    extends AbstractNodeEvaluationSimulator<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(MonteCarloSimulator.class);

    private final int simulationCount;
    private final SplittableRandom random;
    private final SimpleTimer timer = new SimpleTimer();

    public MonteCarloSimulator(int simulationCount, double discountFactor, SplittableRandom random, RewardAggregator<TReward> rewardAggregator) {
        super(rewardAggregator, discountFactor);
        this.simulationCount = simulationCount;
        this.random = random;
    }

    @Override
    protected TReward calcExpectedReward(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> node, TAction firstAction) {
        List<TReward> aggregatedRewardsList = new ArrayList<>();
        timer.startTimer();
        for (int i = 0; i < simulationCount; i++) {
            aggregatedRewardsList.add(runRandomWalkSimulation(node, firstAction));
        }
        timer.stopTimer();
        logger.debug("Running [{}] MonteCarlo simulations for action [{}]. Simulations per second: [{}]", simulationCount, firstAction, timer.samplesPerSec(simulationCount));
        return rewardAggregator.averageReward(aggregatedRewardsList);
    }

    private TReward runRandomWalkSimulation(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> node, TAction firstAction) {
        StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> stateRewardReturn = node.getWrappedState().applyAction(firstAction);
        State<TAction, TReward, TObservation> wrappedState = stateRewardReturn.getState();
        List<TReward> gainedRewards = new ArrayList<>();
        gainedRewards.add(stateRewardReturn.getReward());
        while(!wrappedState.isFinalState()) {
            TAction[] actions = wrappedState.getAllPossibleActions();
            int actionIndex = random.nextInt(actions.length);
            stateRewardReturn = wrappedState.applyAction(actions[actionIndex]);
            wrappedState = stateRewardReturn.getState();
            gainedRewards.add(stateRewardReturn.getReward());

        }
         return rewardAggregator.aggregateDiscount(gainedRewards, discountFactor);
    }
}
