package vahy.paperGenerics.evaluator;

import vahy.api.model.Action;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.utils.ImmutableTriple;
import vahy.utils.RandomDistributionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

public class MonteCarloNodeEvaluator<
    TAction extends Enum<TAction> & Action,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, DoubleVector, TState>>
    extends PaperNodeEvaluator<TAction, TSearchNodeMetadata, TState> {

    protected final SplittableRandom random;
    protected final double discountFactor;
    protected final double[] priorProbabilities;

    @Deprecated // TOTO JE KURVA CELE ZLE
    public MonteCarloNodeEvaluator(SearchNodeFactory<TAction, DoubleVector, TSearchNodeMetadata, TState> searchNodeFactory,
                                   Predictor<TState> knownModel,
                                   TAction[] allPlayerActions,
                                   TAction[] allOpponentActions,
                                   SplittableRandom random,
                                   double discountFactor) {
        super(searchNodeFactory, null, null, knownModel, allPlayerActions, allOpponentActions);
        throw new UnsupportedOperationException("Ok so ... class [" + MonteCarloNodeEvaluator.class + "] is deprecated. Needs to be fixed. Issue: how to sample unknown opponent ");
//        this.random = random;
//        this.discountFactor = discountFactor;
//        this.priorProbabilities = new double[allPlayerActions.length];
//        for (int i = 0; i < priorProbabilities.length; i++) {
//            priorProbabilities[i] = 1.0 / allPlayerActions.length;
//        }
    }

    @Override
    protected int innerEvaluation(SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> node) {

        ImmutableTriple<Double, Boolean, Integer> sampledRewardWithRisk = runRandomWalkSimulation(node);
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
//            evaluateOpponentNode(node, childPriorProbabilities, null);
        }
        return sampledRewardWithRisk.getThird();
    }

    protected ImmutableTriple<Double, Boolean, Integer> runRandomWalkSimulation(SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> node) {
        List<Double> rewardList = new ArrayList<>();
        TState wrappedState = node.getWrappedState();
        var nodeCounter = 0;
        while (!wrappedState.isFinalState()) {
            TAction action = getNextAction(wrappedState);
            StateRewardReturn<TAction, DoubleVector, TState> stateRewardReturn = wrappedState.applyAction(action);
            rewardList.add(stateRewardReturn.getReward());
            wrappedState = stateRewardReturn.getState();
            nodeCounter++;
        }
        return new ImmutableTriple<>(DoubleScalarRewardAggregator.aggregateDiscount(rewardList, discountFactor), wrappedState.isRiskHit(), nodeCounter);
    }

    protected TAction getNextAction(TState wrappedState) {
        if(wrappedState.isPlayerTurn()) {
            TAction[] actions = wrappedState.getAllPossibleActions();
            int actionIndex = random.nextInt(actions.length);
            return actions[actionIndex];
        } else {
            TAction[] actions = wrappedState.getAllPossibleActions();
            if(knownModel != null) {
                double[] probabilities = knownModel.apply(wrappedState);
                return actions[RandomDistributionUtils.getRandomIndexFromDistribution(probabilities, random)];
            } else {
                int actionIndex = random.nextInt(actions.length);
                return actions[actionIndex];
            }
        }
    }

}
