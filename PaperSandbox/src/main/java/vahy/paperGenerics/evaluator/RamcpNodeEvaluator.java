package vahy.paperGenerics.evaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.function.Function;

public class RamcpNodeEvaluator<
    TAction extends Action,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>>
    extends MonteCarloNodeEvaluator<TAction, TOpponentObservation, TSearchNodeMetadata, TState> {

    private final Logger logger = LoggerFactory.getLogger(RamcpNodeEvaluator.class.getName());

    public RamcpNodeEvaluator(SearchNodeFactory<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeFactory,
                              Function<TOpponentObservation, ImmutableTuple<List<TAction>, List<Double>>> opponentApproximator,
                              TAction[] allPlayerActions,
                              TAction[] allOpponentActions,
                              SplittableRandom random,
                              RewardAggregator rewardAggregator,
                              double discountFactor) {
        super(searchNodeFactory, opponentApproximator, allPlayerActions, allOpponentActions, random, rewardAggregator, discountFactor);
    }

    @Override
    protected ImmutableTuple<Double, Boolean> runRandomWalkSimulation(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> node) {
        var parent = node;
        List<Double> rewardList = new ArrayList<>();
        List<SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState>> nodeList = new ArrayList<>();
        TState wrappedState = node.getWrappedState();
        while (!parent.isFinalNode()) {

            initializeChildNodePrioriProbabilityMap(parent);

            TAction action = getNextAction(wrappedState);
            StateRewardReturn<TAction, DoubleVector, TOpponentObservation, TState> stateRewardReturn = wrappedState.applyAction(action);
            var nextNode = searchNodeFactory.createNode(stateRewardReturn, parent, action);
            nodeList.add(nextNode);
            rewardList.add(stateRewardReturn.getReward());

            wrappedState = nextNode.getWrappedState();
            parent = nextNode;
        }
        if(!parent.getWrappedState().isRiskHit()) {
            createSuccessfulBranch(node, nodeList);
        }  else {
            node.getChildNodeMap().clear();
            node.getSearchNodeMetadata().getChildPriorProbabilities().clear();
        }
        return new ImmutableTuple<>(rewardAggregator.aggregateDiscount(rewardList, discountFactor), wrappedState.isRiskHit());
    }

    private void initializeChildNodePrioriProbabilityMap(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> node) {
        var allPossibleActions = node.getWrappedState().getAllPossibleActions();
        var childNodePriorProbabilitiesMap = node.getSearchNodeMetadata().getChildPriorProbabilities();

        if(node.isPlayerTurn()) {
            for (TAction possibleAction : allPossibleActions) {
                childNodePriorProbabilitiesMap.put(possibleAction, priorProbabilities[0]);
            }
        } else {
            var probabilities = opponentApproximator.apply(node.getWrappedState().getOpponentObservation());
            for (int i = 0; i < probabilities.getFirst().size(); i++) {
                childNodePriorProbabilitiesMap.put(probabilities.getFirst().get(i), probabilities.getSecond().get(i));
            }
        }
    }

    private void createSuccessfulBranch(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> node,
                                        List<SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState>> nodeList) {
        if(node.isFinalNode()) {
            return;
        }
        var parent = node;
        for (SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> entryNode : nodeList) {
            addNextNode(parent, entryNode, entryNode.getAppliedAction());
            parent = entryNode;
        }

        var reward = parent.getSearchNodeMetadata().getCumulativeReward();
        var risk = parent.getWrappedState().isRiskHit() ? 1.0 : 0.0;
        parent = parent.getParent();
        if(!parent.isRoot()) {
            while(!parent.equals(node)) {
                updateNode(parent, reward, risk);
                parent = parent.getParent();
            }
        }
    }

    private void addNextNode(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> parent,
                             SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> child,
                             TAction action) {
        TAction[] allPossibleActions = parent.getAllPossibleActions();
        Map<TAction, SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState>> childNodeMap = parent.getChildNodeMap();
        for (TAction nextAction : allPossibleActions) {
            childNodeMap.put(nextAction, action.equals(nextAction) ? child : createSideNode(parent, nextAction));
        }
    }

    private void updateNode(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> updatedNode,
                            double estimatedLeafReward,
                            double estimatedRisk) {
        PaperMetadata<TAction> searchNodeMetadata = updatedNode.getSearchNodeMetadata();
        searchNodeMetadata.increaseVisitCounter();

        if(updatedNode.isFinalNode()) {
            if(searchNodeMetadata.getVisitCounter() == 1) {
                searchNodeMetadata.setSumOfTotalEstimations(0.0);
                searchNodeMetadata.setSumOfRisk(estimatedRisk);
            }
        } else {
            if(searchNodeMetadata.getVisitCounter() == 1) {
                searchNodeMetadata.setSumOfTotalEstimations(searchNodeMetadata.getPredictedReward());
                searchNodeMetadata.setSumOfRisk(estimatedRisk);
            } else {
                searchNodeMetadata.setSumOfTotalEstimations(searchNodeMetadata.getSumOfTotalEstimations() + (estimatedLeafReward - searchNodeMetadata.getCumulativeReward()));
                searchNodeMetadata.setSumOfRisk(searchNodeMetadata.getSumOfRisk() + estimatedRisk);
            }
            searchNodeMetadata.setExpectedReward(searchNodeMetadata.getSumOfTotalEstimations() / searchNodeMetadata.getVisitCounter());
            searchNodeMetadata.setPredictedRisk(searchNodeMetadata.getSumOfRisk() / searchNodeMetadata.getVisitCounter());
        }
    }

    private SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> createSideNode(
        SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> parent,
        TAction nextAction) {
        var stateRewardReturn = parent.applyAction(nextAction);
        var childNode = searchNodeFactory.createNode(stateRewardReturn, parent, nextAction);
        var searchNodeMetadata = childNode.getSearchNodeMetadata();

        searchNodeMetadata.setPredictedReward(0.0);
        searchNodeMetadata.setExpectedReward(0.0);
        searchNodeMetadata.setSumOfRisk(1.0);
        searchNodeMetadata.setPredictedRisk(1.0);
        searchNodeMetadata.setSumOfTotalEstimations(0.0);

        initializeChildNodePrioriProbabilityMap(childNode);

        return childNode;
    }


}
