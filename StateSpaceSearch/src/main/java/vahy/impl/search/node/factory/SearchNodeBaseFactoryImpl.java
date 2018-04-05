package vahy.impl.search.node.factory;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.impl.search.node.SearchNodeImpl;

import java.util.LinkedHashMap;
import java.util.function.Function;

public class SearchNodeBaseFactoryImpl<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>>
    implements SearchNodeFactory<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {

    private final Function<StateRewardReturn<TAction, TReward, TObservation, TState>, TSearchNodeMetadata> searchNodeMetadataFactory;

    public SearchNodeBaseFactoryImpl(Function<StateRewardReturn<TAction, TReward, TObservation, TState>, TSearchNodeMetadata> searchNodeMetadataFactory) {
        this.searchNodeMetadataFactory = searchNodeMetadataFactory;
    }

    @Override
    public SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> createNode(
        StateRewardReturn<TAction, TReward, TObservation, TState> stateRewardReturn,
        SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> parent) {
        return new SearchNodeImpl<>(
            stateRewardReturn.getState(),
            searchNodeMetadataFactory.apply(stateRewardReturn),
            new LinkedHashMap<>());
    }
}
