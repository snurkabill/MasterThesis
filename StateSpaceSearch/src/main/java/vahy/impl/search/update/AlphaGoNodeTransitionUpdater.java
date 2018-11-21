package vahy.impl.search.update;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.impl.search.node.nodeMetadata.AlphaGoNodeMetadata;

public class AlphaGoNodeTransitionUpdater<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TState extends State<TAction, TReward, TObservation, TState>>
    implements NodeTransitionUpdater<TAction, TReward, TObservation, AlphaGoNodeMetadata<TReward>, TState> {


    @Override
    public void applyUpdate(SearchNode<TAction, TReward, TObservation, AlphaGoNodeMetadata<TReward>, TState> evaluatedNode,
                            SearchNode<TAction, TReward, TObservation, AlphaGoNodeMetadata<TReward>, TState> parent,
                            SearchNode<TAction, TReward, TObservation, AlphaGoNodeMetadata<TReward>, TState> child) {
        throw new UnsupportedOperationException();
    }
}
