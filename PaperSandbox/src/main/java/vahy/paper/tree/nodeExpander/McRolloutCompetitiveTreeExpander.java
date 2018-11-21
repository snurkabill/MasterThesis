package vahy.paper.tree.nodeExpander;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.paper.tree.EdgeMetadata;
import vahy.paper.tree.SearchNode;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

public class McRolloutCompetitiveTreeExpander implements NodeExpander {

    private static final Logger logger = LoggerFactory.getLogger(McRolloutCompetitiveTreeExpander.class);

    private final int rolloutCount;
    private final SplittableRandom random;
    private final double discountFactor;
    private final DoubleScalarRewardAggregator rewardAggregator;

    private int nodesExpandedCount = 0;

    public McRolloutCompetitiveTreeExpander(int rolloutCount, SplittableRandom random, double discountFactor, DoubleScalarRewardAggregator rewardAggregator) {
        this.rolloutCount = rolloutCount;
        this.random = random;
        this.discountFactor = discountFactor;
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public void expandNode(SearchNode node) {
        if (node.isFinalNode()) {
            throw new IllegalStateException("Final node cannot be expanded.");
        }
        if(node.isFakeRisk()) {
            node.setFakeRisk(false);
        }

        SearchNode currentNode = node;

        if (node.getChildMap().isEmpty()) {
            firstLevelExpansion(node);
        }

        int depth = 0;
        double riskSum = 0.0;
        double rewardSum = 0.0;
        List<DoubleScalarReward> rewardList = new ArrayList<>();
        ActionType firstAction = null;
        ActionType secondAction = null;
        while(!currentNode.isFinalNode()) {
            depth++;
            if(currentNode.isLeaf()) {
                expandChildren(currentNode);
            }
            ActionType nextAction = selectNextAction(currentNode);
            if(firstAction == null) {
                firstAction = nextAction;
            } else if(secondAction == null) {
                secondAction = nextAction;
            }

            currentNode = currentNode.getChildMap().get(nextAction);
            if(currentNode.getParent() == node) {
                rewardList.add(new DoubleScalarReward(currentNode.getGainedReward().getValue() + node.getCumulativeReward().getValue()));
            } else {
                rewardList.add(currentNode.getGainedReward());
            }
        }

        double leafRisk = currentNode.getWrappedState().isAgentKilled() ? 1.0 : 0.0;
        double leafReward = rewardAggregator.aggregateDiscount(rewardList, discountFactor).getValue();

        logger.trace("Node expanded in depth [{}] with risk [{}] and reward [{}]", depth, leafRisk, leafReward);

        riskSum += leafRisk;
        rewardSum += leafReward;


        if(currentNode.getWrappedState().isAgentKilled()) {
            if(secondAction != null) {
                SearchNode firstChild = node.getChildMap().get(firstAction);
                SearchNode subChildNodeSearched = firstChild.getChildMap().get(secondAction);
                subChildNodeSearched.getChildMap().clear();
                subChildNodeSearched.getEdgeMetadataMap().clear();
                firstChild.setFakeRisk(false);
                subChildNodeSearched.setFakeRisk(false);
            }
            node.setEvaluated();
        } else {
            currentNode.setEstimatedRisk(0.0d);
            while(currentNode != node) {
                currentNode.setFakeRisk(false);
                if(!currentNode.isAlreadyEvaluated()) {
                    currentNode.setEvaluated();
                }
                SearchNode parent = currentNode.getParent();
                ActionType appliedAction = currentNode.getAppliedParentAction();

                currentNode.setTotalVisitCounter(currentNode.getTotalVisitCounter() + 1);
                EdgeMetadata metadata = parent.getEdgeMetadataMap().get(appliedAction);
                metadata.setVisitCount(metadata.getVisitCount() + 1);

                metadata.setTotalActionValue(metadata.getTotalActionValue() + leafReward);
                metadata.setTotalRiskValue(metadata.getTotalRiskValue() + leafRisk);
                metadata.setMeanActionValue(metadata.getTotalActionValue() / metadata.getVisitCount());
                metadata.setMeanRiskValue(metadata.getTotalRiskValue() / metadata.getVisitCount());

                currentNode = currentNode.getParent();

                currentNode.setEstimatedReward(new DoubleScalarReward(metadata.getMeanActionValue()));
                currentNode.setEstimatedRisk(metadata.getMeanRiskValue());
            }
        }
        node.setEstimatedRisk(riskSum);
        node.setEstimatedReward(new DoubleScalarReward(rewardSum));
        node.setFakeRisk(false);
    }

    @Override
    public int getNodesExpandedCount() {
        return nodesExpandedCount;
    }

//    private void deletePriorInit(SearchNode node) {
//        node.getParent().getEdgeMetadataMap().get(node.getAppliedAction()).setTotalRiskValue(0.0d);
//        node.getParent().getEdgeMetadataMap().get(node.getAppliedAction()).setTotalRiskValue(0.0d);
//        node.setEstimatedRisk(0.0);
//        node.setEstimatedReward(new DoubleScalarReward(0.0d));
//    }

    private void setPriorProbabilities(SearchNode currentNode) {
        if(currentNode.isOpponentTurn()) {
            ImmutableTuple<List<ActionType>, List<Double>> environmentActionsWithProbabilities = currentNode.getWrappedState().environmentActionsWithProbabilities();
            List<ActionType> actions = environmentActionsWithProbabilities.getFirst();
            List<Double> probabilities = environmentActionsWithProbabilities.getSecond();
            for (int i = 0; i < actions.size(); i++) {
                currentNode.getEdgeMetadataMap().get(actions.get(i)).setPriorProbability(probabilities.get(i));
            }
        } else {
            int actionCount = currentNode.getWrappedState().getAllPossibleActions().length;
            for (Map.Entry<ActionType, EdgeMetadata> entry : currentNode.getEdgeMetadataMap().entrySet()) {
                entry.getValue().setPriorProbability(1.0 / actionCount);
            }
        }
    }

    private ActionType selectNextAction(SearchNode currentNode) {
        if(currentNode.isOpponentTurn()) {
            ImmutableTuple<List<ActionType>, List<Double>> environmentActionsWithProbabilities = currentNode.getWrappedState().environmentActionsWithProbabilities();
            List<ActionType> actions = environmentActionsWithProbabilities.getFirst();
            List<Double> probabilities = environmentActionsWithProbabilities.getSecond();
            int index = RandomDistributionUtils.getRandomIndexFromDistribution(probabilities, random);
            return actions.get(index);
        } else {
            ActionType[] actions = currentNode.getWrappedState().getAllPossibleActions();
            return actions[random.nextInt(actions.length)];
        }
    }

    private void firstLevelExpansion(SearchNode node) {
        innerNodeExpansion(node);
        expandChildren(node);
    }

    private void expandChildren(SearchNode node) {
        node.getChildMap().entrySet().stream().filter(x -> !x.getValue().isFinalNode()).forEach(x -> innerNodeExpansion(x.getValue()));
    }

    private void innerNodeExpansion(SearchNode node) {
        if (node.isFinalNode()) {
            throw new IllegalStateException("Final node cannot be expanded.");
        }
        nodesExpandedCount++;
        ActionType[] allActions = node.getWrappedState().getAllPossibleActions();
        logger.trace("Expanding node [{}] with possible actions: [{}] ", node, Arrays.toString(allActions));

        if(node.getChildMap().size() != 0) {
            throw new IllegalStateException("Node was already expanded");
        }

        for (ActionType action : allActions) {
            StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation,
                                State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> stateRewardReturn = node.getWrappedState().applyAction(action);
            logger.trace("Expanding node [{}] with action [{}] resulting in reward [{}]", node, action, stateRewardReturn.getReward().toPrettyString());
            SearchNode newNode = new SearchNode((ImmutableStateImpl) stateRewardReturn.getState(), node, action, stateRewardReturn.getReward());
            EdgeMetadata edgeMetadata = new EdgeMetadata();
            edgeMetadata.setMeanActionValue(0.0d);
            edgeMetadata.setMeanRiskValue(1.0d);
            newNode.setEstimatedReward(new DoubleScalarReward(0.0));
            newNode.setEstimatedRisk(1.0d);
            newNode.setFakeRisk(true);
            node.getChildMap().put(action, newNode);
            node.getEdgeMetadataMap().put(action, edgeMetadata);
       }
        setPriorProbabilities(node);
        node.setEvaluated();
    }
}
