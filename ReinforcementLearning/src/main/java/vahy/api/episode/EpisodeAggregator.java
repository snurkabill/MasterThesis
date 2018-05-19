package vahy.api.episode;

import vahy.api.model.reward.Reward;

import java.util.List;

public interface EpisodeAggregator<TReward extends Reward> {

    List<List<TReward>> runSimulation();
}
