package vahy.impl.search.MCTS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.utils.ImmutableTuple;

import java.util.Arrays;
import java.util.Map;

public abstract class MCTSEvaluator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends MCTSMetadata,
    TState extends State<TAction, TObservation, TState>>
    implements NodeEvaluator<TAction, TObservation, TSearchNodeMetadata, TState> {

    protected static final Logger logger = LoggerFactory.getLogger(MCTSPredictionEvaluator.class);
    protected static final boolean TRACE_ENABLED = logger.isTraceEnabled();

    private final SearchNodeFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeFactory;

    public MCTSEvaluator(SearchNodeFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeFactory) {
        this.searchNodeFactory = searchNodeFactory;
    }

    @Override
    public int evaluateNode(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectedNode) {
        if(selectedNode.isFinalNode()) {
            throw new IllegalStateException("Final node cannot be expanded.");
        }
        TAction[] allPossibleActions = selectedNode.getAllPossibleActions();
        if(TRACE_ENABLED) {
            logger.trace("Expanding node [{}] with possible actions: [{}] ", selectedNode, Arrays.toString(allPossibleActions));
        }
        Map<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> childNodeMap = selectedNode.getChildNodeMap();
        for (TAction nextAction : allPossibleActions) {
            var stateRewardReturn = selectedNode.applyAction(nextAction);
            childNodeMap.put(nextAction, searchNodeFactory.createNode(stateRewardReturn, selectedNode, nextAction));
        }
        var estimatesWithNodeCount = estimateRewards(selectedNode);
        var estimatedRewards = estimatesWithNodeCount.getFirst();

        TSearchNodeMetadata searchNodeMetadata = selectedNode.getSearchNodeMetadata();
        var predictedRewards = searchNodeMetadata.getPredictedReward();
        System.arraycopy(estimatedRewards, 0, predictedRewards, 0, predictedRewards.length);
        if(!selectedNode.isFinalNode()) {
            selectedNode.unmakeLeaf();
        }
        return estimatesWithNodeCount.getSecond();
    }

    protected abstract ImmutableTuple<double[], Integer> estimateRewards(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectedNode);
}
