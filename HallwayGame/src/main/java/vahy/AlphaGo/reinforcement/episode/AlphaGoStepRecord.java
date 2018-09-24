package vahy.AlphaGo.reinforcement.episode;

import vahy.impl.model.reward.DoubleScalarReward;

public class AlphaGoStepRecord {

    private final double[] priorProbabilities;
    private final double[] policyProbabilities;
    private final DoubleScalarReward rewardPredicted;

    public AlphaGoStepRecord(double[] priorProbabilities, double[] policyProbabilities, DoubleScalarReward rewardPredicted) {
        this.priorProbabilities = priorProbabilities;
        this.policyProbabilities = policyProbabilities;
        this.rewardPredicted = rewardPredicted;
    }

    public double[] getPriorProbabilities() {
        return priorProbabilities;
    }

    public double[] getPolicyProbabilities() {
        return policyProbabilities;
    }

    public DoubleScalarReward getRewardPredicted() {
        return rewardPredicted;
    }
}
