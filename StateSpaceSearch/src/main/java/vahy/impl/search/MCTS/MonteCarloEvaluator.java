package vahy.impl.search.MCTS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

public class MonteCarloEvaluator<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends MonteCarloTreeSearchMetadata<TReward>,
    TState extends State<TAction, TReward, TObservation, TState>>
    implements NodeEvaluator<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(MonteCarloEvaluator.class);

    private final SearchNodeFactory<TAction, TReward, TObservation, TSearchNodeMetadata, TState> searchNodeFactory;
    private final SplittableRandom random;
    private final RewardAggregator<TReward> rewardAggregator;
    private final double discountFactor;
    private final int rolloutCount;

    public MonteCarloEvaluator(SearchNodeFactory<TAction, TReward, TObservation, TSearchNodeMetadata, TState> searchNodeFactory,
                               SplittableRandom random,
                               RewardAggregator<TReward> rewardAggregator,
                               double discountFactor,
                               int rolloutCount) {
        this.searchNodeFactory = searchNodeFactory;
        this.random = random;
        this.rewardAggregator = rewardAggregator;
        this.discountFactor = discountFactor;
        this.rolloutCount = rolloutCount;
    }

    @Override
    public void evaluateNode(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> selectedNode) {
        if(selectedNode.isFinalNode()) {
            throw new IllegalStateException("Final node cannot be expanded.");
        }
        TAction[] allPossibleActions = selectedNode.getAllPossibleActions();
        logger.trace("Expanding node [{}] with possible actions: [{}] ", selectedNode, Arrays.toString(allPossibleActions));
        Map<TAction, SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> childNodeMap = selectedNode.getChildNodeMap();
        for (TAction nextAction : allPossibleActions) {
            StateRewardReturn<TAction, TReward, TObservation, TState> stateRewardReturn = selectedNode.applyAction(nextAction);
            childNodeMap.put(nextAction, searchNodeFactory.createNode(stateRewardReturn, selectedNode, nextAction));
        }
        TReward rewardPrediction = runRollouts(selectedNode);
        TSearchNodeMetadata searchNodeMetadata = selectedNode.getSearchNodeMetadata();
        searchNodeMetadata.setPredictedReward(rewardPrediction);
    }

    private TReward runRollouts(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> node) {
        List<TReward> rewardList = new ArrayList<>();
        for (int i = 0; i < rolloutCount; i++) {
            rewardList.add(runRandomWalkSimulation(node));
        }
        return rewardAggregator.averageReward(rewardList);
    }

    private TReward runRandomWalkSimulation(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> node) {
        List<TReward> rewardList = new ArrayList<>();
        TState wrappedState = node.getWrappedState();
        while (!wrappedState.isFinalState()) {
            TAction[] actions = wrappedState.getAllPossibleActions();
            int actionIndex = random.nextInt(actions.length);
            StateRewardReturn<TAction, TReward, TObservation, TState> stateRewardReturn = wrappedState.applyAction(actions[actionIndex]);
            rewardList.add(stateRewardReturn.getReward());
            wrappedState = stateRewardReturn.getState();
        }
        return rewardAggregator.aggregateDiscount(rewardList, discountFactor);
    }
}
