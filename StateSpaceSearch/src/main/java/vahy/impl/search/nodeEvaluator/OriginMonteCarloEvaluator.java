package vahy.impl.search.nodeEvaluator;

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
import vahy.impl.search.node.nodeMetadata.MCTSNodeMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

public class OriginMonteCarloEvaluator<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends MCTSNodeMetadata<TReward>,
    TState extends State<TAction, TReward, TObservation, TState>>
    implements NodeEvaluator<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(OriginMonteCarloEvaluator.class);

    private final SearchNodeFactory<TAction, TReward, TObservation, TSearchNodeMetadata, TState> searchNodeFactory;
    private final SplittableRandom random;
    private final RewardAggregator<TReward> rewardAggregator;

    public OriginMonteCarloEvaluator(SearchNodeFactory<TAction, TReward, TObservation, TSearchNodeMetadata, TState> searchNodeFactory,
                                     SplittableRandom random,
                                     RewardAggregator<TReward> rewardAggregator) {
        this.searchNodeFactory = searchNodeFactory;
        this.random = random;
        this.rewardAggregator = rewardAggregator;
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
        TAction nextRandomAction = allPossibleActions[random.nextInt(allPossibleActions.length)];
        logger.trace("Running random simulation for action [{}]", nextRandomAction);
        SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> nextNode = selectedNode.getChildNodeMap().get(nextRandomAction);
        TReward expectedReward = runRandomWalkSimulation(nextNode);
        TSearchNodeMetadata searchNodeMetadata = selectedNode.getSearchNodeMetadata();
        searchNodeMetadata.setSumOfTotalEstimations(expectedReward);
        searchNodeMetadata.setEstimatedTotalReward(expectedReward);
        searchNodeMetadata.increaseVisitCounter();
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
        return rewardAggregator.aggregate(rewardList);
    }
}
