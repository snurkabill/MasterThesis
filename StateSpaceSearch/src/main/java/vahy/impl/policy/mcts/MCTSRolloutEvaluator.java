package vahy.impl.policy.mcts;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.impl.model.reward.DoubleVectorRewardAggregator;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public class MCTSRolloutEvaluator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends MCTSMetadata,
    TState extends State<TAction, TObservation, TState>>
    extends MCTSEvaluator<TAction, TObservation, TSearchNodeMetadata, TState> {

    private final SplittableRandom random;
    private final double discountFactor;
    private final int rolloutCount;

    public MCTSRolloutEvaluator(SearchNodeFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeFactory, SplittableRandom random, double discountFactor, int rolloutCount) {
        super(searchNodeFactory);
        this.random = random;
        this.discountFactor = discountFactor;
        this.rolloutCount = rolloutCount;
    }

    @Override
    protected ImmutableTuple<double[], Integer> estimateRewards(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectedNode) {
        if(selectedNode.isFinalNode()) {
            return new ImmutableTuple<>(new double[selectedNode.getSearchNodeMetadata().getExpectedReward().length], 0);
        }
        List<ImmutableTuple<double[], Integer>> rewardList = new ArrayList<>(rolloutCount);

        for (int i = 0; i < rolloutCount; i++) {
            rewardList.add(runRandomWalkSimulation(selectedNode));
        }
        return new ImmutableTuple<>(
            DoubleVectorRewardAggregator.averageReward(rewardList.stream().map(ImmutableTuple::getFirst).collect(Collectors.toList())),
            rewardList.stream().mapToInt(ImmutableTuple::getSecond).sum());
    }

    private ImmutableTuple<double[], Integer> runRandomWalkSimulation(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node) {
        List<double[]> rewardList = new ArrayList<>();
        int stateCounter = 0;
        StateWrapper<TAction, TObservation, TState> wrappedState = node.getStateWrapper();
        while (!wrappedState.isFinalState()) {
            TAction[] actions = wrappedState.getAllPossibleActions();
            int actionIndex = random.nextInt(actions.length);
            StateWrapperRewardReturn<TAction, TObservation, TState> stateRewardReturn = wrappedState.applyAction(actions[actionIndex]);
            rewardList.add(stateRewardReturn.getAllPlayerRewards());
            wrappedState = stateRewardReturn.getState();
            stateCounter += 1;
        }
        return new ImmutableTuple<>(DoubleVectorRewardAggregator.aggregateDiscount(rewardList, discountFactor), stateCounter);
    }

}
