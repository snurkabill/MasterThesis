package vahy.paperGenerics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.function.Function;

public class RamcpNodeEvaluator<
    TAction extends Action,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, DoubleReward>,
    TState extends PaperState<TAction, DoubleReward, DoubleVector, TOpponentObservation, TState>>
    extends MonteCarloNodeEvaluator<TAction, TOpponentObservation, TSearchNodeMetadata, TState> {

    private final Logger logger = LoggerFactory.getLogger(RamcpNodeEvaluator.class.getName());

    public RamcpNodeEvaluator(SearchNodeFactory<TAction, DoubleReward, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeFactory,
                              Function<TOpponentObservation, ImmutableTuple<List<TAction>, List<Double>>> opponentApproximator,
                              TAction[] allPlayerActions,
                              TAction[] allOpponentActions,
                              SplittableRandom random,
                              RewardAggregator<DoubleReward> rewardAggregator,
                              double discountFactor) {
        super(searchNodeFactory, opponentApproximator, allPlayerActions, allOpponentActions, random, rewardAggregator, discountFactor);
    }

    @Override
    protected ImmutableTuple<DoubleReward, Boolean> runRandomWalkSimulation(SearchNode<TAction, DoubleReward, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> node) {
        var parent = node;
        List<DoubleReward> rewardList = new ArrayList<>();
        List<SearchNode<TAction, DoubleReward, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState>> nodeList = new ArrayList<>();
        TState wrappedState = node.getWrappedState();
        while (!parent.isFinalNode()) {

            initializeChildNodePrioriProbabilityMap(parent);

            TAction action = getNextAction(wrappedState);
            StateRewardReturn<TAction, DoubleReward, DoubleVector, TOpponentObservation, TState> stateRewardReturn = wrappedState.applyAction(action);
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

    private void initializeChildNodePrioriProbabilityMap(SearchNode<TAction, DoubleReward, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> node) {
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

    private void createSuccessfulBranch(SearchNode<TAction, DoubleReward, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> node,
                                        List<SearchNode<TAction, DoubleReward, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState>> nodeList) {
        if(node.isFinalNode()) {
            return;
        }
        var parent = node;
        for (SearchNode<TAction, DoubleReward, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> entryNode : nodeList) {
            addNextNode(parent, entryNode, entryNode.getAppliedAction());
            parent = entryNode;
        }

        var reward = parent.getSearchNodeMetadata().getCumulativeReward().getValue();
        var risk = parent.getWrappedState().isRiskHit() ? 1.0 : 0.0;
        parent = parent.getParent();
        if(!parent.isRoot()) {
            while(!parent.equals(node)) {
                updateNode(parent, reward, risk);
                parent = parent.getParent();
            }
        }
    }

    private void addNextNode(SearchNode<TAction, DoubleReward, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> parent,
                             SearchNode<TAction, DoubleReward, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> child,
                             TAction action) {
        TAction[] allPossibleActions = parent.getAllPossibleActions();
        Map<TAction, SearchNode<TAction, DoubleReward, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState>> childNodeMap = parent.getChildNodeMap();
        for (TAction nextAction : allPossibleActions) {
            childNodeMap.put(nextAction, action.equals(nextAction) ? child : createSideNode(parent, nextAction));
        }
    }

    private void updateNode(SearchNode<TAction, DoubleReward, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> updatedNode,
                            double estimatedLeafReward,
                            double estimatedRisk) {
        PaperMetadata<TAction, DoubleReward> searchNodeMetadata = updatedNode.getSearchNodeMetadata();
        searchNodeMetadata.increaseVisitCounter();

        if(updatedNode.isFinalNode()) {
            if(searchNodeMetadata.getVisitCounter() == 1) {
                searchNodeMetadata.setSumOfTotalEstimations(new DoubleReward(0.0));
                searchNodeMetadata.setSumOfRisk(estimatedRisk);
            }
        } else {
            if(searchNodeMetadata.getVisitCounter() == 1) {
                searchNodeMetadata.setSumOfTotalEstimations(new DoubleReward(searchNodeMetadata.getPredictedReward().getValue()));
                searchNodeMetadata.setSumOfRisk(estimatedRisk);
            } else {
                searchNodeMetadata.setSumOfTotalEstimations(new DoubleReward(searchNodeMetadata.getSumOfTotalEstimations().getValue() + (estimatedLeafReward - searchNodeMetadata.getCumulativeReward().getValue())));
                searchNodeMetadata.setSumOfRisk(searchNodeMetadata.getSumOfRisk() + estimatedRisk);
            }
            searchNodeMetadata.setExpectedReward(new DoubleReward(searchNodeMetadata.getSumOfTotalEstimations().getValue() / searchNodeMetadata.getVisitCounter()));
            searchNodeMetadata.setPredictedRisk(searchNodeMetadata.getSumOfRisk() / searchNodeMetadata.getVisitCounter());
        }
    }

    private SearchNode<TAction, DoubleReward, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> createSideNode(
        SearchNode<TAction, DoubleReward, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> parent,
        TAction nextAction) {
        var stateRewardReturn = parent.applyAction(nextAction);
        var childNode = searchNodeFactory.createNode(stateRewardReturn, parent, nextAction);
        var searchNodeMetadata = childNode.getSearchNodeMetadata();

        searchNodeMetadata.setPredictedReward(new DoubleReward(0.0));
        searchNodeMetadata.setExpectedReward(new DoubleReward(0.0));
        searchNodeMetadata.setSumOfRisk(1.0);
        searchNodeMetadata.setPredictedRisk(1.0);
        searchNodeMetadata.setSumOfTotalEstimations(new DoubleReward(0.0));

        initializeChildNodePrioriProbabilityMap(childNode);

        return childNode;
    }


}
