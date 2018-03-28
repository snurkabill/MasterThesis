package vahy.impl.search.node.factory;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.impl.search.node.SearchNodeImpl;

import java.util.LinkedHashMap;

public class SearchNodBaseFactoryImpl<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward>,
    TState extends State<TAction, TReward, TObservation>>
    implements SearchNodeFactory<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {




    @Override
    public SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> createNode(
        StateRewardReturn<TAction, TReward, TObservation, TState> stateRewardReturn,
        SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> parent) {
        return new SearchNodeImpl<>(
            stateRewardReturn.getState(),
            null,
            new LinkedHashMap<>());
    }
}
