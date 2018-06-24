package vahy.api.model.reward;

public interface RewardFactory<TReward extends Reward> {

    TReward fromNumericVector(double[] vector);
}
