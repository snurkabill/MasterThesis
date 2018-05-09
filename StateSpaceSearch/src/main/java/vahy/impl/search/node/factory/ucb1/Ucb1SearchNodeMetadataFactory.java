package vahy.impl.search.node.factory.ucb1;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1SearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1StateActionMetadata;

import java.util.LinkedHashMap;

public class Ucb1SearchNodeMetadataFactory<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation>
    implements SearchNodeMetadataFactory<TAction, TReward, TObservation, Ucb1StateActionMetadata<TReward>> {

    @Override
    public SearchNodeMetadata<TAction, TReward, Ucb1StateActionMetadata<TReward>> createSearchNodeMetadata(StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> stateRewardReturn) {
        return new Ucb1SearchNodeMetadata<>(stateRewardReturn.getReward(), new LinkedHashMap<>());
    }
}
