package vahy.api.policy;

import vahy.api.model.Action;

import java.util.Arrays;

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

    public double getExpectedReward() {
        return predictedReward;
    }

    public double[] getDistribution() {
        return distribution;
    }

    @Override
    public String toString() {
        return "PlayingDistribution{" +
            "action=" + playedAction +
            ", predictedReward=" + predictedReward +
            ", distribution=" + Arrays.toString(distribution) +
            '}';
    }
}
