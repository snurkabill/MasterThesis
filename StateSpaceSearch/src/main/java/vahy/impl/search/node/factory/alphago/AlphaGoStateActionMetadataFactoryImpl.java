package vahy.impl.search.node.factory.alphago;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.alphago.AlphaGoNodeMetadata;
import vahy.impl.search.node.nodeMetadata.alphago.AlphaGoStateActionMetadata;

import java.util.LinkedHashMap;

public class AlphaGoStateActionMetadataFactoryImpl<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends AlphaGoNodeMetadata<TAction, TReward>>
    implements SearchNodeMetadataFactory<TAction, TReward, TObservation, AlphaGoStateActionMetadata<TReward>, TSearchNodeMetadata, State<TAction, TReward, TObservation>> {

    @Override
    public SearchNodeMetadata<TAction, TReward, AlphaGoStateActionMetadata<TReward>> createSearchNodeMetadata(StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> stateRewardReturn) {
        return new AlphaGoNodeMetadata<>(stateRewardReturn.getReward(), new LinkedHashMap<>());
    }
}
