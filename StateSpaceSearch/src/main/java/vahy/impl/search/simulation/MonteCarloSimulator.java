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
import vahy.api.search.simulation.NodeEvaluationSimulator;
import vahy.timer.SimpleTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

public class MonteCarloSimulator<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>> implements NodeEvaluationSimulator<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(MonteCarloSimulator.class);

    private final int simulationCount;
    private final double discountFactor;
    private final SplittableRandom random;
    private final RewardAggregator<TReward> rewardAggregator;
    private final SimpleTimer timer = new SimpleTimer();

    public MonteCarloSimulator(int simulationCount, double discountFactor, SplittableRandom random, RewardAggregator<TReward> rewardAggregator) {
        this.simulationCount = simulationCount;
        this.discountFactor = discountFactor;
        this.random = random;
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public void calculateMetadataEstimation(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> expandedNode) {
        if(expandedNode.isFinalNode()) {
            throw new IllegalStateException("Final node cannot be evaluated anymore");
        }
        TSearchNodeMetadata searchNodeMetadata = expandedNode.getSearchNodeMetadata();
        for (Map.Entry<TAction, SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState>> entry : expandedNode.getChildNodeMap().entrySet()) {
            timer.startTimer();
            TReward expectedReward = calcExpectedReward(expandedNode, entry.getKey());
            entry.getValue().getSearchNodeMetadata().setEstimatedTotalReward(expectedReward);
            searchNodeMetadata.getStateActionMetadataMap().get(entry.getKey()).setEstimatedTotalReward(expectedReward);
            timer.stopTimer();
            logger.debug("Running [{}] MonteCarlo simulations for action [{}]. Simulations per second: [{}]", simulationCount, entry.getKey(), timer.samplesPerSec(simulationCount));
        }
        expandedNode.getSearchNodeMetadata().setEstimatedTotalReward(
            rewardAggregator.averageReward(searchNodeMetadata
                    .getStateActionMetadataMap()
                    .values()
                    .stream()
                    .map(StateActionMetadata::getEstimatedTotalReward)
            )
        );
    }

    private TReward calcExpectedReward(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> node, TAction firstAction) {
        List<TReward> aggregatedRewardsList = new ArrayList<>();
        for (int i = 0; i < simulationCount; i++) {
            aggregatedRewardsList.add(runRandomWalkSimulation(node, firstAction));
        }
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
