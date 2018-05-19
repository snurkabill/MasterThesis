package vahy.search;

import vahy.api.model.State;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.node.nodeMetadata.AbstractStateActionMetadata;
import vahy.utils.ImmutableTuple;

import java.util.List;
import java.util.Map;

public abstract class MaximizingRewardGivenProbabilities {

    protected DoubleScalarReward resolveReward(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> state, Map<ActionType, AbstractStateActionMetadata<DoubleScalarReward>> stateActionMap) {
        if(state.isOpponentTurn()) {
            ImmutableTuple<List<ActionType>, List<Double>> actionsWithProbabilities = ((ImmutableStateImpl) state).environmentActionsWithProbabilities();
            double sum = 0.0;
            for (int i = 0; i < actionsWithProbabilities.getFirst().size(); i++) {
                sum += stateActionMap.get(actionsWithProbabilities.getFirst().get(i)).getEstimatedTotalReward().getValue() *
                    actionsWithProbabilities.getSecond().get(i);
            }
            return new DoubleScalarReward(sum);
        } else {
            return stateActionMap
                .values()
                .stream()
                .map(StateActionMetadata::getEstimatedTotalReward)
                .max(Comparable::compareTo)
                .orElseThrow(() -> new IllegalStateException("Children should be always expanded when doing transition update"));
        }
    }
}
