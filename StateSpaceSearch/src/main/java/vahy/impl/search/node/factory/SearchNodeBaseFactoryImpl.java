package vahy.impl.search.node.factory;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.NodeMetadata;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.impl.search.node.SearchNodeImpl;

import java.util.EnumMap;
import java.util.Map;

public class SearchNodeBaseFactoryImpl<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TSearchNodeMetadata extends NodeMetadata, TState extends State<TAction, TObservation, TState>>
    implements SearchNodeFactory<TAction, TObservation, TSearchNodeMetadata, TState> {

    private final Class<TAction> actionClass;
    private final SearchNodeMetadataFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeMetadataFactory;

    public SearchNodeBaseFactoryImpl(Class<TAction> actionClass, SearchNodeMetadataFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeMetadataFactory) {
        this.actionClass = actionClass;
        this.searchNodeMetadataFactory = searchNodeMetadataFactory;
    }

    @Override
    public SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> createNode(StateWrapperRewardReturn<TAction, TObservation, TState> stateRewardReturn,
                                                                                     SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> parent,
                                                                                     TAction action)
    {
        var searchNodeMetadata = searchNodeMetadataFactory.createSearchNodeMetadata(parent, stateRewardReturn, action);
        return new SearchNodeImpl<>(stateRewardReturn.getState(), searchNodeMetadata, new EnumMap<>(actionClass), parent, action);
    }

    @Override
    public SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> createNode(StateWrapper<TAction, TObservation, TState> initialState,
                                                                                     TSearchNodeMetadata metadata,
                                                                                     Map<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> childNodeMap) {
        return new SearchNodeImpl<>(initialState, metadata, childNodeMap);
    }

    @Override
    public SearchNodeMetadataFactory<TAction, TObservation, TSearchNodeMetadata, TState> getSearchNodeMetadataFactory() {
        return searchNodeMetadataFactory;
    }
}
