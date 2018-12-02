package vahy.paperGenerics.reinforcement.episode;

import vahy.impl.model.reward.DoubleReward;

public class StepRecord<TReward extends DoubleReward> {

    private final double[] priorProbabilities;
    private final double[] policyProbabilities;
    private final TReward rewardPredicted;
    private final double risk;

    public StepRecord(double[] priorProbabilities, double[] policyProbabilities, TReward rewardPredicted, double risk) {
        this.priorProbabilities = priorProbabilities;
        this.policyProbabilities = policyProbabilities;
        this.rewardPredicted = rewardPredicted;
        this.risk = risk;
    }

    public double[] getPriorProbabilities() {
        return priorProbabilities;
    }

    public double[] getPolicyProbabilities() {
        return policyProbabilities;
    }

    public TReward getRewardPredicted() {
        return rewardPredicted;
    }

    public double getRisk() {
        return risk;
    }
}
