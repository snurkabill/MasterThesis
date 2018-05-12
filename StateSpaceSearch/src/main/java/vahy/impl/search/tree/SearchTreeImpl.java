package vahy.impl.search.tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.api.search.nodeExpander.NodeExpander;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.api.search.simulation.NodeEvaluationSimulator;
import vahy.api.search.tree.SearchTree;
import vahy.api.search.update.TreeUpdater;
import vahy.impl.model.ImmutableStateRewardReturnTuple;

public class SearchTreeImpl<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>>
    implements SearchTree<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(SearchTreeImpl.class);
    private SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> root;
    private final NodeSelector<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> nodeSelector;
    private final NodeExpander<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> nodeExpander;
    private final TreeUpdater<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> treeUpdater;
    private final NodeEvaluationSimulator<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> nodeEvaluationSimulator;

    private int totalNodesExpanded = 0;
    private int totalNodesCreated = 0; // should be 1 for root
    private int maxBranchingFactor = 0;

    public SearchTreeImpl(
        SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> root,
        NodeSelector<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> nodeSelector,
        NodeExpander<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> nodeExpander,
        TreeUpdater<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> treeUpdater,
        NodeEvaluationSimulator<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> nodeEvaluationSimulator) {
        this.root = root;
        this.nodeSelector = nodeSelector;
        this.nodeExpander = nodeExpander;
        this.treeUpdater = treeUpdater;
        this.nodeEvaluationSimulator = nodeEvaluationSimulator;
        this.nodeSelector.setNewRoot(root);
    }

    @Override
    public boolean updateTree() {
        SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> selectedNodeForExpansion = nodeSelector.selectNextNode();
        if(selectedNodeForExpansion == null) {
            return false;
        }
        if(!selectedNodeForExpansion.isFinalNode()) {
            logger.debug("Selected node [{}] is not final node, expanding", selectedNodeForExpansion);
            expandNode(selectedNodeForExpansion);
            nodeSelector.addNodes(selectedNodeForExpansion.getChildNodeMap().values());
        }
        nodeEvaluationSimulator.calculateMetadataEstimation(selectedNodeForExpansion);
        treeUpdater.updateTree(selectedNodeForExpansion);
        return true;
    }

    @Override
    public TAction[] getAllPossibleActions() {
        return this.root.getWrappedState().getAllPossibleActions();
    }

    @Override
    public StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> applyAction(TAction action) {
        if(root.isFinalNode()) {
            throw new IllegalStateException("Can't apply action [" + action +"] on final state");
        }
        if(root.isLeaf()) {
            logger.debug("Trying to apply action on not expanded tree branch. Forcing expansion.");
            expandNode(root);
        }
        TReward reward = root.getSearchNodeMetadata().getStateActionMetadataMap().get(action).getGainedReward();
        root = root.getChildNodeMap().get(action);
        root.makeRoot();
        nodeSelector.setNewRoot(root);
        resetTreeStatistics();
        return new ImmutableStateRewardReturnTuple<>(root.getWrappedState(), reward);
    }

    @Override
    public State<TAction, TReward, TObservation> deepCopy() {
        throw new UnsupportedOperationException("Deep copy on search tree is not yet defined nad maybe won't be since it's not really needed");
    }

    @Override
    public TObservation getObservation() {
        return root.getWrappedState().getObservation();
    }

    @Override
    public String readableStringRepresentation() {
        return root.getWrappedState().readableStringRepresentation();
    }

    @Override
    public boolean isOpponentTurn() {
        return root.isOpponentTurn();
    }

    @Override
    public boolean isFinalState() {
        return root.isFinalNode();
    }

    @Override
    public SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> getRoot() {
        return root;
    }

    private void expandNode(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> selectedNodeForExpansion) {
        nodeExpander.expandNode(selectedNodeForExpansion);
        totalNodesExpanded++;
        int branchingNodesCount = selectedNodeForExpansion.getChildNodeMap().size();
        if(branchingNodesCount > maxBranchingFactor) {
            maxBranchingFactor = branchingNodesCount;
        }
        totalNodesCreated += branchingNodesCount;
    }

    private void resetTreeStatistics() {
        totalNodesCreated = 0;
        totalNodesExpanded = 0;
        maxBranchingFactor = Integer.MIN_VALUE;
    }

    public int getTotalNodesExpanded() {
        return totalNodesExpanded;
    }

    public int getTotalNodesCreated() {
        return totalNodesCreated;
    }

    public int getMaxBranchingFactor() {
        return maxBranchingFactor;
    }

    public double calculateAverageBranchingFactor() {
        return totalNodesCreated / (double) totalNodesExpanded;
    }
}
