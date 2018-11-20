package vahy.paper.tree.nodeEvaluator;

import vahy.environment.ActionType;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.paper.reinforcement.TrainableApproximator;
import vahy.paper.tree.SearchNode;
import vahy.utils.ImmutableTuple;

import java.util.List;

public class ApproximatorBasedNodeEvaluator extends NodeEvaluator {

    private final TrainableApproximator trainableApproximator;

    public ApproximatorBasedNodeEvaluator(TrainableApproximator trainableApproximator) {
        this.trainableApproximator = trainableApproximator;
    }

    public TrainableApproximator getTrainableApproximator() {
        return trainableApproximator;
    }

    @Override
    public void innerEvaluateNode(SearchNode node) {
        if(node.isAlreadyEvaluated()) {
            throw new IllegalStateException("Node was already evaluated");
        }
        double[] prediction = trainableApproximator.apply(node.getWrappedState().getObservation());
        node.setEstimatedReward(new DoubleScalarReward(prediction[Q_VALUE_INDEX]));
        node.setEstimatedRisk(prediction[RISK_VALUE_INDEX]);
        if(node.getWrappedState().isAgentTurn()) {
            ActionType[] playerActions = ActionType.playerActions;
            for (int i = 0; i < playerActions.length; i++) {
                node.getEdgeMetadataMap().get(playerActions[i]).setPriorProbability(prediction[i + POLICY_START_INDEX]);
            }
        } else {
            ImmutableTuple<List<ActionType>, List<Double>> environmentActionsWithProbabilities = node.getWrappedState().environmentActionsWithProbabilities();

            for (int i = 0; i < environmentActionsWithProbabilities.getFirst().size(); i++) {
                node.getEdgeMetadataMap().get(environmentActionsWithProbabilities.getFirst().get(i)).setPriorProbability(environmentActionsWithProbabilities.getSecond().get(i));
            }
        }
        node.setEvaluated();
    }
}