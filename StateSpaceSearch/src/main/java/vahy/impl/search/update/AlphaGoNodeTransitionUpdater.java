package vahy.impl.search.update;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.impl.search.node.nodeMetadata.alphago.AlphaGoNodeMetadata;
import vahy.impl.search.node.nodeMetadata.alphago.AlphaGoStateActionMetadata;

public class AlphaGoNodeTransitionUpdater<TAction extends Action, TReward extends Reward, TObservation extends Observation>
    implements NodeTransitionUpdater<TAction, TReward, TObservation, AlphaGoStateActionMetadata<TReward>, AlphaGoNodeMetadata<TAction, TReward>, State<TAction, TReward, TObservation>> {

    @Override
    public void applyUpdate(SearchNode<TAction, TReward, TObservation, AlphaGoStateActionMetadata<TReward>, AlphaGoNodeMetadata<TAction, TReward>, State<TAction, TReward, TObservation>> parent,
                            SearchNode<TAction, TReward, TObservation, AlphaGoStateActionMetadata<TReward>, AlphaGoNodeMetadata<TAction, TReward>, State<TAction, TReward, TObservation>> child,
                            TAction action) {

    }
}
