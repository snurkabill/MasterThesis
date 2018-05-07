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

    public MonteCarloSimulator(int simulationCount, double discountFactor, SplittableRandom random, RewardAggregator<TReward> rewardAggregator) {
        this.simulationCount = simulationCount;
        this.discountFactor = discountFactor;
        this.random = random;
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public void calculateMetadataEstimation(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> expandedNode) {
        if(expandedNode.isFinalNode()) {
            TAction appliedAction = expandedNode.getAppliedParentAction();
            expandedNode.getSearchNodeMetadata().setEstimatedTotalReward(expandedNode.getParent().getSearchNodeMetadata().getStateActionMetadataMap().get(appliedAction).getGainedReward());
            return;
        }
        TSearchNodeMetadata searchNodeMetadata = expandedNode.getSearchNodeMetadata();
        for (Map.Entry<TAction, SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState>> entry : expandedNode.getChildNodeMap().entrySet()) {
            TReward expectedReward = calcExpectedReward(entry.getValue());
            searchNodeMetadata.getStateActionMetadataMap().get(entry.getKey()).setEstimatedTotalReward(expectedReward);
        }
        expandedNode.getSearchNodeMetadata().setEstimatedTotalReward(
            rewardAggregator.aggregate(
                rewardAggregator.averageReward(searchNodeMetadata
                    .getStateActionMetadataMap()
                    .values()
                    .stream()
                    .map(StateActionMetadata::getEstimatedTotalReward)),
                rewardAggregator.averageReward(searchNodeMetadata
                    .getStateActionMetadataMap()
                    .values()
                    .stream()
                    .map(StateActionMetadata::getGainedReward)))
            );
    }

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
//            TReward averageReward = calcExpectedReward(entry.getValue());
//
//            searchNodeMetadata.getStateActionMetadataMap().get(entry.getKey()).getGainedReward();
//
//        }
//    }

    private TReward calcExpectedReward(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> node) {
        List<TReward> aggregatedRewardsList = new ArrayList<>();
        for (int i = 0; i < simulationCount; i++) {
            aggregatedRewardsList.add(runRandomWalkSimulation(node));
        }
        return rewardAggregator.averageReward(aggregatedRewardsList);
    }

    private TReward runRandomWalkSimulation(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> node) {
        State<TAction, TReward, TObservation> wrappedState = node.getWrappedState();
        List<TReward> gainedRewards = new ArrayList<>();

        while(!wrappedState.isFinalState()) {
            TAction[] actions = wrappedState.getAllPossibleActions();
            int actionIndex = random.nextInt(actions.length);
            StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> stateRewardReturn = wrappedState.applyAction(actions[actionIndex]);
            wrappedState = stateRewardReturn.getState();
            gainedRewards.add(stateRewardReturn.getReward());
        }
        return rewardAggregator.aggregate(gainedRewards);
    }
//
//    private StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> doNextStep(State<TAction, TReward, TObservation> wrappedState) {
//
//    }



}
