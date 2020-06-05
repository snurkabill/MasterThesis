package vahy.impl.search.node.factory;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.NodeMetadata;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.impl.search.node.SearchNodeImpl;

import java.util.EnumMap;

public class SearchNodeBaseFactoryImpl<TAction extends Enum<TAction> & Action, TObservation extends Observation, TSearchNodeMetadata extends NodeMetadata, TState extends State<TAction, TObservation, TState>>
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
                                                                                     TAction action) {
        return new SearchNodeImpl<>(
            stateRewardReturn.getState(),
            searchNodeMetadataFactory.createSearchNodeMetadata(parent, stateRewardReturn, action),
            new EnumMap<>(actionClass),
            parent,
            action);
    }
}
