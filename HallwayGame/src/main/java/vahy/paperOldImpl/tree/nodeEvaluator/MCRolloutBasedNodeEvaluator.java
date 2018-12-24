package vahy.paperOldImpl.tree.nodeEvaluator;

import vahy.api.model.StateRewardReturn;
import vahy.environment.HallwayAction;
import vahy.environment.state.HallwayStateImpl;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.paperOldImpl.tree.SearchNode;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public class MCRolloutBasedNodeEvaluator extends NodeEvaluator {

    private final SplittableRandom random;
    private final int rolloutCount;
    private final DoubleScalarRewardAggregator doubleScalarRewardAggregator = new DoubleScalarRewardAggregator();
    private final double discountFactor;

    public MCRolloutBasedNodeEvaluator(SplittableRandom random, int rolloutCount, double discountFactor) {
        this.random = random;
        this.rolloutCount = rolloutCount;
        this.discountFactor = discountFactor;
    }

    @Override
    protected void innerEvaluateNode(SearchNode node) {
        if(node.isAlreadyEvaluated()) {
            throw new IllegalStateException("Node was already evaluated");
        }
        double rewardSum = 0.0;
        double riskSum = 0.0;
        for (int i = 0; i < rolloutCount; i++) {
            ImmutableTuple<Double, Double> result = runRandomWalkSimulation(node);
            rewardSum += result.getFirst();
            riskSum += result.getSecond();
        }
        node.setEstimatedReward(new DoubleReward(rewardSum / rolloutCount));
        node.setEstimatedRisk(riskSum / rolloutCount);
        if(node.getWrappedState().isAgentTurn()) {
            HallwayAction[] playerActions = HallwayAction.playerActions;
            for (int i = 0; i < playerActions.length; i++) {
                node.getEdgeMetadataMap().get(playerActions[i]).setPriorProbability(1.0 / playerActions.length);
            }
        } else {
            ImmutableTuple<List<HallwayAction>, List<Double>> environmentActionsWithProbabilities = node.getWrappedState().environmentActionsWithProbabilities();
            for (int i = 0; i < environmentActionsWithProbabilities.getFirst().size(); i++) {
                node.getEdgeMetadataMap().get(environmentActionsWithProbabilities.getFirst().get(i)).setPriorProbability(environmentActionsWithProbabilities.getSecond().get(i));
            }
        }
        node.setEvaluated();
    }


    private ImmutableTuple<Double, Double> runRandomWalkSimulation(SearchNode node) {
        List<DoubleReward> gainedRewards = new ArrayList<>();
        HallwayStateImpl wrappedState = node.getWrappedState();
        while (!wrappedState.isFinalState()) {
            HallwayAction selectedAction = selectNextAction(wrappedState);
            StateRewardReturn<HallwayAction, DoubleReward, DoubleVector, HallwayStateImpl> stateRewardReturn = wrappedState.applyAction(selectedAction);
            wrappedState = stateRewardReturn.getState();
            gainedRewards.add(stateRewardReturn.getReward());
        }
        return new ImmutableTuple<>(doubleScalarRewardAggregator.aggregateDiscount(gainedRewards, discountFactor).getValue(), wrappedState.isAgentKilled() ? 1.0 : 0.0);
    }

    private HallwayAction selectNextAction(HallwayStateImpl state) {
        if(state.isOpponentTurn()) {
            ImmutableTuple<List<HallwayAction>, List<Double>> environmentActionsWithProbabilities = state.environmentActionsWithProbabilities();
            List<HallwayAction> actions = environmentActionsWithProbabilities.getFirst();
            List<Double> probabilities = environmentActionsWithProbabilities.getSecond();
            int index = RandomDistributionUtils.getRandomIndexFromDistribution(probabilities, random);
            return actions.get(index);
        } else {
            HallwayAction[] actions = state.getAllPossibleActions();
            int actionIndex = random.nextInt(actions.length);
            return actions[actionIndex];
        }
    }
}
