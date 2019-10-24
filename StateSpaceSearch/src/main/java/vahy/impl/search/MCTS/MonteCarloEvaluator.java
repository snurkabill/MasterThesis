package vahy.impl.search.MCTS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

public class MonteCarloEvaluator<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends MonteCarloTreeSearchMetadata,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements NodeEvaluator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(MonteCarloEvaluator.class);

    private final SearchNodeFactory<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeFactory;
    private final SplittableRandom random;
    private final double discountFactor;
    private final int rolloutCount;

    public MonteCarloEvaluator(SearchNodeFactory<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeFactory,
                               SplittableRandom random,
                               double discountFactor,
                               int rolloutCount) {
        this.searchNodeFactory = searchNodeFactory;
        this.random = random;
        this.discountFactor = discountFactor;
        this.rolloutCount = rolloutCount;
    }

    @Override
    public int evaluateNode(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> selectedNode) {
        if(selectedNode.isFinalNode()) {
            throw new IllegalStateException("Final node cannot be expanded.");
        }
        TAction[] allPossibleActions = selectedNode.getAllPossibleActions();
        logger.trace("Expanding node [{}] with possible actions: [{}] ", selectedNode, Arrays.toString(allPossibleActions));
        Map<TAction, SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> childNodeMap = selectedNode.getChildNodeMap();
        for (TAction nextAction : allPossibleActions) {
            StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState> stateRewardReturn = selectedNode.applyAction(nextAction);
            childNodeMap.put(nextAction, searchNodeFactory.createNode(stateRewardReturn, selectedNode, nextAction));
        }
        var rewardPredictionNodeCounter = runRollouts(selectedNode);
        TSearchNodeMetadata searchNodeMetadata = selectedNode.getSearchNodeMetadata();
        searchNodeMetadata.setPredictedReward(rewardPredictionNodeCounter.getFirst());
        return rewardPredictionNodeCounter.getSecond();
    }

    private ImmutableTuple<Double, Integer> runRollouts(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node) {
        List<ImmutableTuple<Double, Integer>> rewardList = new ArrayList<>();
        for (int i = 0; i < rolloutCount; i++) {
            rewardList.add(runRandomWalkSimulation(node));
        }
        return new ImmutableTuple<>(DoubleScalarRewardAggregator.averageReward(rewardList.stream().map(ImmutableTuple::getFirst)), rewardList.stream().mapToInt(ImmutableTuple::getSecond).sum());
    }

    private ImmutableTuple<Double, Integer> runRandomWalkSimulation(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node) {
        List<Double> rewardList = new ArrayList<>();
        int stateCounter = 0;
        TState wrappedState = node.getWrappedState();
        while (!wrappedState.isFinalState()) {
            TAction[] actions = wrappedState.getAllPossibleActions();
            int actionIndex = random.nextInt(actions.length);
            StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState> stateRewardReturn = wrappedState.applyAction(actions[actionIndex]);
            rewardList.add(stateRewardReturn.getReward());
            wrappedState = stateRewardReturn.getState();
            stateCounter += 1;
        }
        return new ImmutableTuple<>(DoubleScalarRewardAggregator.aggregateDiscount(rewardList, discountFactor), stateCounter);
    }
}
