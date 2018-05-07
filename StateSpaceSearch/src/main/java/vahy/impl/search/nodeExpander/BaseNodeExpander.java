package vahy.impl.search.nodeExpander;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.api.search.nodeExpander.NodeExpander;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class BaseNodeExpander<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>>
    implements NodeExpander<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, State<TAction, TReward, TObservation>> {

    private static final Logger logger = LoggerFactory.getLogger(BaseNodeExpander.class);
    private final SearchNodeFactory<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, State<TAction, TReward, TObservation>> searchNodeFactory;
    private final Function<StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>>, TStateActionMetadata> stateActionMetadataFactory;

    public BaseNodeExpander(
        SearchNodeFactory<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, State<TAction, TReward, TObservation>> searchNodeFactory,
        Function<StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>>, TStateActionMetadata> stateActionMetadataFactory) {
        this.searchNodeFactory = searchNodeFactory;
        this.stateActionMetadataFactory = stateActionMetadataFactory;
    }

    @Override
    public void expandNode(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, State<TAction, TReward, TObservation>> node) {
        if(node.isFinalNode()) {
            throw new IllegalStateException("Final node cannot be expanded.");
        }

        TAction[] allPossibleActions = node.getAllPossibleActions();
        logger.debug("Expanding node [{}] with possible actions: [{}] ", node, Arrays.toString(allPossibleActions));
        Map<TAction, SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, State<TAction, TReward, TObservation>>> childNodeMap = node.getChildNodeMap();
        Map<TAction, TStateActionMetadata> stateActionMetadataMap = node.getSearchNodeMetadata().getStateActionMetadataMap();
        for (TAction action : allPossibleActions) {
            StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> stateRewardReturn = node.applyAction(action);
            logger.debug("Expanding node [{}] with action [{}] resulting in reward [{}]", node, action, stateRewardReturn.getReward().toPrettyString());
            childNodeMap.put(action, searchNodeFactory.createNode(stateRewardReturn, node, action));
            stateActionMetadataMap.put(action, stateActionMetadataFactory.apply(stateRewardReturn));
        }
    }
}
