package vahy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.NodeMetadata;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;

import java.util.List;

public class ExplicitSearchTreeBuilder<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TSearchNodeMetadata extends NodeMetadata, TState extends State<TAction, TObservation, TState>> {

    private final SearchNodeFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeFactory;

    public ExplicitSearchTreeBuilder(SearchNodeFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeFactory) {
        this.searchNodeFactory = searchNodeFactory;
    }

    private void applyPath(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> root, List<TAction> actionPath) {
        var node = root;
        for (TAction action : actionPath) {
            if(node.getChildNodeMap().containsKey(action)) {
                node = node.getChildNodeMap().get(action);
            } else {
                var stateReward = node.applyAction(action);
                var newNode = searchNodeFactory.createNode(stateReward, node, action);
                node.getChildNodeMap().put(action, newNode);
                node = newNode;
            }
        }
    }

    public void buildSearchTree(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> root, List<List<TAction>> actionList) {
        for (List<TAction> actionPath : actionList) {
            applyPath(root, actionPath);
        }
    }

}
