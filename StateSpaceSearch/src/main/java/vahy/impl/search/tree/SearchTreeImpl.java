package vahy.impl.search.tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.api.search.nodeExpander.NodeExpander;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.api.search.simulation.NodeEvaluationSimulator;
import vahy.api.search.tree.SearchTree;
import vahy.api.search.update.TreeUpdater;

public class SearchTreeImpl<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>>
    implements SearchTree<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(SearchTreeImpl.class);
    private final SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> root;
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
        this.nodeSelector.addNode(root);
    }

    @Override
    public boolean updateTree() {
        SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> selectedNodeForExpansion = nodeSelector.selectNextNode();
        totalNodesExpanded++;
        if(selectedNodeForExpansion == null) {
            return false;
        }
        if(!selectedNodeForExpansion.isFinalNode()) {
            logger.debug("Selected node [{}] is not final node, expanding", selectedNodeForExpansion);
            nodeExpander.expandNode(selectedNodeForExpansion);
            int branchingNodesCount = selectedNodeForExpansion.getChildNodeMap().size();
            if(branchingNodesCount > maxBranchingFactor) {
                maxBranchingFactor = branchingNodesCount;
            }
            totalNodesCreated += branchingNodesCount;
            nodeSelector.addNodes(selectedNodeForExpansion.getChildNodeMap().values());
        }
        nodeEvaluationSimulator.calculateMetadataEstimation(selectedNodeForExpansion);
        treeUpdater.updateTree(selectedNodeForExpansion);
        return true;
    }

    @Override
    public SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> getRoot() {
        return root;
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
