package vahy.search;

import vahy.environment.HallwayAction;
import vahy.environment.state.HallwayStateImpl;
import vahy.impl.model.reward.DoubleReward;
import vahy.utils.ImmutableTuple;

import java.util.List;
import java.util.Map;

public abstract class MaximizingRewardGivenProbabilities {

    protected DoubleReward resolveReward(HallwayStateImpl state, Map<HallwayAction, DoubleReward> actionRewardMap) {
        if(state.isOpponentTurn()) {
            ImmutableTuple<List<HallwayAction>, List<Double>> actionsWithProbabilities = state.getOpponentObservation().getProbabilities();
            double sum = 0.0;
            for (int i = 0; i < actionsWithProbabilities.getFirst().size(); i++) {
                sum += actionRewardMap.get(actionsWithProbabilities.getFirst().get(i)).getValue() * actionsWithProbabilities.getSecond().get(i);
            }
            return new DoubleReward(sum);
        } else {
            return actionRewardMap
                .values()
                .stream()
                .max(Comparable::compareTo)
                .orElseThrow(() -> new IllegalStateException("Children should be always expanded when doing transition update"));
        }
    }
}
