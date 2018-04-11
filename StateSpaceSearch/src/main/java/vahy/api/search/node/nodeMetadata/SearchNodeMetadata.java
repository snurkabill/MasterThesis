package vahy.api.search.node.nodeMetadata;

import vahy.api.model.Action;
import vahy.api.model.reward.Reward;

import java.util.Map;

public interface SearchNodeMetadata<TAction extends Action, TReward extends Reward, TStateActionMetadata extends StateActionMetadata<TReward>> {

    Map<TAction, TStateActionMetadata> getStateActionMetadataMap();

    TReward getCumulativeReward();

    TReward getEstimatedTotalReward();

    void setEstimatedTotalReward(TReward reward);

}
