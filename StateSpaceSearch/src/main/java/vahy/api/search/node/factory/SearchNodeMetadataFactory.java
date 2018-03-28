package vahy.api.search.node.factory;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.search.node.SearchNodeMetadata;

public interface SearchNodeMetadataFactory<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward>,
    TState extends State<TAction, TReward, TObservation>>{

    TSearchNodeMetadata createSearchNodeMetadata(StateRewardReturn<TAction, TReward, TObservation, TState> stateRewardReturn);

}
