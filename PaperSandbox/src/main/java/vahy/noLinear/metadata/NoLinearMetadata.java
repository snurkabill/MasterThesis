package vahy.noLinear.metadata;

import vahy.api.model.Action;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadata;

public class NoLinearMetadata<TAction extends Action<TAction>> extends MonteCarloTreeSearchMetadata {

    public NoLinearMetadata(double cumulativeReward, double gainedReward, double predictedReward) {
        super(cumulativeReward, gainedReward, predictedReward);
    }
}
