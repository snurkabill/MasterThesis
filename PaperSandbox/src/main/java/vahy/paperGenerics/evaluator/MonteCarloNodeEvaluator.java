package vahy.paperGenerics.evaluator;

import vahy.api.model.Action;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.function.Function;

public class MonteCarloNodeEvaluator<
    TAction extends Action,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>>
    extends PaperNodeEvaluator<TAction, TOpponentObservation, TSearchNodeMetadata, TState> {

    protected final SplittableRandom random;
    protected final RewardAggregator rewardAggregator;
    protected final double discountFactor;
    protected final double[] priorProbabilities;

    public MonteCarloNodeEvaluator(SearchNodeFactory<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeFactory,
                                   Function<TOpponentObservation, ImmutableTuple<List<TAction>, List<Double>>> opponentApproximator,
                                   TAction[] allPlayerActions,
                                   TAction[] allOpponentActions,
                                   SplittableRandom random,
                                   RewardAggregator rewardAggregator,
                                   double discountFactor) {
        super(searchNodeFactory, null, opponentApproximator, allPlayerActions, allOpponentActions);
        this.random = random;
        this.rewardAggregator = rewardAggregator;
        this.discountFactor = discountFactor;
        this.priorProbabilities = new double[allPlayerActions.length];
        for (int i = 0; i < priorProbabilities.length; i++) {
            priorProbabilities[i] = 1.0 / allPlayerActions.length;
        }
    }

    @Override
    protected void innerEvaluation(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> node) {

        ImmutableTuple<Double, Boolean> sampledRewardWithRisk = runRandomWalkSimulation(node);
        node.getSearchNodeMetadata().increaseVisitCounter();
        node.getSearchNodeMetadata().setPredictedReward(sampledRewardWithRisk.getFirst());
        node.getSearchNodeMetadata().setExpectedReward(sampledRewardWithRisk.getFirst());
        node.getSearchNodeMetadata().setSumOfTotalEstimations(sampledRewardWithRisk.getFirst());
        if(!node.isFinalNode()) {
            double risk = sampledRewardWithRisk.getSecond() ? 1.0 : 0.0;
            node.getSearchNodeMetadata().setPredictedRisk(risk);
            node.getSearchNodeMetadata().setSumOfRisk(risk);
        }
        Map<TAction, Double> childPriorProbabilities = node.getSearchNodeMetadata().getChildPriorProbabilities();
        if(node.getWrappedState().isPlayerTurn()) {
            for (int i = 0; i < allPlayerActions.length; i++) {
                childPriorProbabilities.put(allPlayerActions[i], (priorProbabilities[i]));
            }
        } else {
            evaluateOpponentNode(node, childPriorProbabilities);
        }
    }

    protected ImmutableTuple<Double, Boolean> runRandomWalkSimulation(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> node) {
        List<Double> rewardList = new ArrayList<>();
        TState wrappedState = node.getWrappedState();
        while (!wrappedState.isFinalState()) {
            TAction action = getNextAction(wrappedState);
            StateRewardReturn<TAction, DoubleVector, TOpponentObservation, TState> stateRewardReturn = wrappedState.applyAction(action);
            rewardList.add(stateRewardReturn.getReward());
            wrappedState = stateRewardReturn.getState();
        }
        return new ImmutableTuple<>(rewardAggregator.aggregateDiscount(rewardList, discountFactor), wrappedState.isRiskHit());
    }

    protected TAction getNextAction(TState wrappedState) {
        if(wrappedState.isPlayerTurn()) {
            TAction[] actions = wrappedState.getAllPossibleActions();
            int actionIndex = random.nextInt(actions.length);
            return actions[actionIndex];
        } else {
            var probabilities = opponentpredictor.apply(wrappedState.getOpponentObservation());
            return probabilities.getFirst().get(RandomDistributionUtils.getRandomIndexFromDistribution(probabilities.getSecond(), random));
        }
    }

}
