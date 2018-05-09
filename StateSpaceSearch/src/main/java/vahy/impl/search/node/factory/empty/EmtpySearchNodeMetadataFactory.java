package vahy.impl.search.node.factory.empty;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.AbstractStateActionMetadata;
import vahy.impl.search.node.nodeMetadata.empty.EmptySearchNodeMetadata;

import java.util.LinkedHashMap;

public class EmtpySearchNodeMetadataFactory<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation>
    implements SearchNodeMetadataFactory<TAction, TReward, TObservation, AbstractStateActionMetadata<TReward>> {

    @Override
    public SearchNodeMetadata<TAction, TReward, AbstractStateActionMetadata<TReward>> createSearchNodeMetadata(StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> stateRewardReturn) {
        return new EmptySearchNodeMetadata<>(stateRewardReturn.getReward(), new LinkedHashMap<>());
    }
}
