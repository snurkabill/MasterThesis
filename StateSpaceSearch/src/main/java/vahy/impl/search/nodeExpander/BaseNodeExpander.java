package vahy.impl.search.nodeExpander;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.api.search.nodeExpander.NodeExpander;
import vahy.api.search.simulation.NodeEvaluationSimulator;

import java.util.Map;

public class BaseNodeExpander<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>>
    implements NodeExpander<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, State<TAction, TReward, TObservation>> {

    private final SearchNodeFactory<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, State<TAction, TReward, TObservation>> searchNodeFactory;
    private final NodeEvaluationSimulator<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, State<TAction, TReward, TObservation>> nodeEvaluationSimulator;

    public BaseNodeExpander(
        SearchNodeFactory<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, State<TAction, TReward, TObservation>> searchNodeFactory,
        NodeEvaluationSimulator<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, State<TAction, TReward, TObservation>> nodeEvaluationSimulator) {
        this.searchNodeFactory = searchNodeFactory;
        this.nodeEvaluationSimulator = nodeEvaluationSimulator;
    }

    @Override
    public void expandNode(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, State<TAction, TReward, TObservation>> node) {
        if(node.isFinalNode()) {
            throw new IllegalStateException("Final node cannot be expanded.");
        }
        TAction[] allPossibleActions = node.getAllPossibleActions();
        Map<TAction, SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, State<TAction, TReward, TObservation>>> childNodeMap = node.getChildNodeMap();
        for (TAction action : allPossibleActions) {
            childNodeMap.put(action, searchNodeFactory.createNode(node.applyAction(action), node));
        }
        nodeEvaluationSimulator.calculateMetadataEstimation(node);
    }
}
