package vahy.impl.search.nodeExpander;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.nodeExpander.NodeExpander;

import java.util.Map;

public class AbstractNodeExpander<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward>,
    TState extends State<TAction, TReward, TObservation>>
    implements NodeExpander<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {



    @Override
    public void expandNode(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> node) {
        TAction[] allPossibleActions = node.getAllPossibleActions();
        Map<TAction, SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> childNodeMap = node.getChildNodeMap();
        for (TAction action : allPossibleActions) {
            StateRewardReturn<TReward, State<TAction, TReward, TObservation>> stateRewardReturn = node.applyAction(action);



        }


    }
}
