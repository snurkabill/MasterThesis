package vahy.api.episode;

import vahy.api.model.reward.Reward;

public interface StepRecord<TReward extends Reward> {

    TReward getRewardPredicted();

}
