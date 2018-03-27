package vahy.api.search.node;

import vahy.api.model.Action;
import vahy.api.model.Reward;

import java.util.Map;

public interface SearchNodeMetadata<TAction extends Action, TReward extends Reward> {

    Map<TAction, StateActionMetadata> getStateActionMetadataMap();

}
