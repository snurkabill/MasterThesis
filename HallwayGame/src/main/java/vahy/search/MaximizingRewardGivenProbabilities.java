package vahy.search;

import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.utils.ImmutableTuple;

import java.util.List;
import java.util.Map;

public abstract class MaximizingRewardGivenProbabilities {

    protected DoubleScalarReward resolveReward(ImmutableStateImpl state, Map<ActionType, DoubleScalarReward> actionRewardMap) {
        if(state.isOpponentTurn()) {
            ImmutableTuple<List<ActionType>, List<Double>> actionsWithProbabilities = state.environmentActionsWithProbabilities();
            double sum = 0.0;
            for (int i = 0; i < actionsWithProbabilities.getFirst().size(); i++) {
                sum += actionRewardMap.get(actionsWithProbabilities.getFirst().get(i)).getValue() * actionsWithProbabilities.getSecond().get(i);
            }
            return new DoubleScalarReward(sum);
        } else {
            return actionRewardMap
                .values()
                .stream()
                .max(Comparable::compareTo)
                .orElseThrow(() -> new IllegalStateException("Children should be always expanded when doing transition update"));
        }
    }
}
