package vahy.impl.search.node.factory;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.empty.EmptySearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.empty.EmptyStateActionMetadata;

import java.util.LinkedHashMap;

public class EmtpySearchNodeMetadataFactory<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends EmptySearchNodeMetadata<TAction, TReward>>
    implements SearchNodeMetadataFactory<TAction, TReward, TObservation, EmptyStateActionMetadata<TReward>, TSearchNodeMetadata, State<TAction, TReward, TObservation>> {

    @Override
    public SearchNodeMetadata<TAction, TReward, EmptyStateActionMetadata<TReward>> createSearchNodeMetadata(StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> stateRewardReturn) {
        return new EmptySearchNodeMetadata<>(stateRewardReturn.getReward(), new LinkedHashMap<>());
    }
}
