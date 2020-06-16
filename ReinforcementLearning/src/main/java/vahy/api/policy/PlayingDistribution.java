package vahy.api.policy;

import vahy.api.model.Action;

public class PlayingDistribution<TAction extends Enum<TAction> & Action>
{
    private final TAction playedAction;
    private final double predictedReward;
    private final double[] distribution;

    public PlayingDistribution(TAction playedAction, double predictedReward, double[] distribution) {
        this.playedAction = playedAction;
        this.predictedReward = predictedReward;
        this.distribution = distribution;
    }

    public TAction getPlayedAction() {
        return playedAction;
    }

    public double getPredictedReward() {
        return predictedReward;
    }

    public double[] getDistribution() {
        return distribution;
    }
}
