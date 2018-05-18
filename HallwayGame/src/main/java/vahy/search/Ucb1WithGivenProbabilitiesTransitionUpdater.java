package vahy.search;

import vahy.api.model.State;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1SearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1StateActionMetadata;
import vahy.utils.ImmutableTuple;

import java.util.List;

public class Ucb1WithGivenProbabilitiesTransitionUpdater implements NodeTransitionUpdater<
    ActionType,
    DoubleScalarReward,
    DoubleVectorialObservation,
    Ucb1StateActionMetadata<DoubleScalarReward>,
    Ucb1SearchNodeMetadata<ActionType, DoubleScalarReward>,
    State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> {

    private final double discountFactor;
    private final RewardAggregator<DoubleScalarReward> rewardAggregator;

    public Ucb1WithGivenProbabilitiesTransitionUpdater(double discountFactor, RewardAggregator<DoubleScalarReward> rewardAggregator) {
        this.discountFactor = discountFactor;
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public void applyUpdate(SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, Ucb1StateActionMetadata<DoubleScalarReward>, Ucb1SearchNodeMetadata<ActionType, DoubleScalarReward>, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> parent, SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, Ucb1StateActionMetadata<DoubleScalarReward>, Ucb1SearchNodeMetadata<ActionType, DoubleScalarReward>, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> child, ActionType action) {
        Ucb1SearchNodeMetadata<ActionType, DoubleScalarReward> parentSearchNodeMetadata = parent.getSearchNodeMetadata();
        Ucb1StateActionMetadata<DoubleScalarReward> stateActionMetadata = parentSearchNodeMetadata.getStateActionMetadataMap().get(action);

        stateActionMetadata.setEstimatedTotalReward(new DoubleScalarReward(rewardAggregator.aggregateDiscount(
            stateActionMetadata.getGainedReward(),
            child.getSearchNodeMetadata().getEstimatedTotalReward(),
            discountFactor).getValue()));
        double parentCumulativeEstimates = parentSearchNodeMetadata.getEstimatedTotalReward().getValue() * (parentSearchNodeMetadata.getVisitCounter() - 1);

        DoubleScalarReward newParentCumulativeEstimate = resolveReward(parent);

        double sum = parentCumulativeEstimates + newParentCumulativeEstimate.getValue();
        parentSearchNodeMetadata.setEstimatedTotalReward(new DoubleScalarReward(sum / parentSearchNodeMetadata.getVisitCounter()));
    }

    private DoubleScalarReward resolveReward(SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, Ucb1StateActionMetadata<DoubleScalarReward>, Ucb1SearchNodeMetadata<ActionType, DoubleScalarReward>, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> parent) {
        if(parent.isOpponentTurn()) {
            ImmutableTuple<List<ActionType>, List<Double>> actionsWithProbabilities = ((ImmutableStateImpl) parent.getWrappedState()).environmentActionsWithProbabilities();
            double sum = 0.0;
            for (int i = 0; i < actionsWithProbabilities.getFirst().size(); i++) {
                sum += parent.getSearchNodeMetadata().getStateActionMetadataMap().get(actionsWithProbabilities.getFirst().get(i)).getEstimatedTotalReward().getValue() *
                    actionsWithProbabilities.getSecond().get(i);
            }
            return new DoubleScalarReward(sum);
        } else {
            return parent.getSearchNodeMetadata()
                .getStateActionMetadataMap()
                .values()
                .stream()
                .map(StateActionMetadata::getEstimatedTotalReward)
                .max(Comparable::compareTo)
                .orElseThrow(() -> new IllegalStateException("Children should be always expanded when doing transition update"));
        }
    }

}
