package vahy.paper.tree.nodeEvaluator;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.paper.tree.SearchNode;
import vahy.utils.ImmutableTuple;

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
        node.setEstimatedReward(new DoubleScalarReward(rewardSum / rolloutCount));
        node.setEstimatedRisk(riskSum / rolloutCount);
        if(node.getWrappedState().isAgentTurn()) {
            ActionType[] playerActions = ActionType.playerActions;
            for (int i = 0; i < playerActions.length; i++) {
                node.getEdgeMetadataMap().get(playerActions[i]).setPriorProbability(1.0 / playerActions.length);
            }
        } else {
            ImmutableTuple<List<ActionType>, List<Double>> environmentActionsWithProbabilities = node.getWrappedState().environmentActionsWithProbabilities();
            for (int i = 0; i < environmentActionsWithProbabilities.getFirst().size(); i++) {
                node.getEdgeMetadataMap().get(environmentActionsWithProbabilities.getFirst().get(i)).setPriorProbability(environmentActionsWithProbabilities.getSecond().get(i));
            }
        }
        node.setEvaluated();
    }


    private ImmutableTuple<Double, Double> runRandomWalkSimulation(SearchNode node) {
        List<DoubleScalarReward> gainedRewards = new ArrayList<>();
        State<ActionType, DoubleScalarReward, DoubleVectorialObservation> wrappedState = node.getWrappedState();
        while (!wrappedState.isFinalState()) {
            ActionType selectedAction = selectNextAction((ImmutableStateImpl) wrappedState);
            StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> stateRewardReturn = wrappedState.applyAction(selectedAction);
            wrappedState = stateRewardReturn.getState();
            gainedRewards.add(stateRewardReturn.getReward());
        }
        return new ImmutableTuple<>(
            doubleScalarRewardAggregator.aggregateDiscount(gainedRewards, discountFactor).getValue(),
            ((ImmutableStateImpl) wrappedState).isAgentKilled() ? 1.0 : 0.0);
    }

    private ActionType selectNextAction(ImmutableStateImpl state) {
        if(state.isOpponentTurn()) {
            ImmutableTuple<List<ActionType>, List<Double>> environmentActionsWithProbabilities = state.environmentActionsWithProbabilities();
            List<ActionType> actions = environmentActionsWithProbabilities.getFirst();
            List<Double> probabilities = environmentActionsWithProbabilities.getSecond();
            double rand = random.nextDouble();
            double cumulativeSum = 0.0d;

            for (int i = 0; i < probabilities.size(); i++) {
                cumulativeSum += probabilities.get(i);
                if(rand < cumulativeSum) {
                    return actions.get(i);
                }
            }
            throw new IllegalStateException("Numerically unstable probability calculation");
        } else {
            ActionType[] actions = state.getAllPossibleActions();
            int actionIndex = random.nextInt(actions.length);
            return actions[actionIndex];
        }
    }
}
