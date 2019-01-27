package vahy.impl.search.tree;


import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.RankDir;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizException;
import guru.nidi.graphviz.model.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.api.search.tree.SearchTree;
import vahy.api.search.update.TreeUpdater;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.utils.ImmutableTuple;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Link.to;

public class SearchTreeImpl<
    TAction extends Action,
    TReward extends Reward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata<TReward>,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    implements SearchTree<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(SearchTreeImpl.class);

    private SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> root;
    private final NodeSelector<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeSelector;
    private final NodeEvaluator<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeEvaluator;
    private final TreeUpdater<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> treeUpdater;

    private int totalNodesExpanded = 0;
    private int totalNodesCreated = 0; // should be 1 for root
    private int maxBranchingFactor = 0;

    public SearchTreeImpl(
        SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> root,
        NodeSelector<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeSelector,
        TreeUpdater<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> treeUpdater,
        NodeEvaluator<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeEvaluator) {
        this.root = root;
        this.nodeSelector = nodeSelector;
        this.treeUpdater = treeUpdater;
        this.nodeEvaluator = nodeEvaluator;
        this.nodeSelector.setNewRoot(root);

        expandTreeToNextPlayerLevel();
    }

    protected void expandTreeToNextPlayerLevel() {
        if(root.isFinalNode()) {
            throw new IllegalArgumentException("Cannot expand final node");
        }
        if(root.getChildNodeMap().size() == 0) {
            logger.debug("Expanding root since it is not final node and has no children expanded");
            expandAndEvaluateNode(root);
            treeUpdater.updateTree(root);
        }
        var queue = root.getChildNodeStream().filter(SearchNode::isOpponentTurn).collect(Collectors.toCollection(LinkedList::new));
        while(!queue.isEmpty()) {
            var node = queue.pop();
            if(!node.isFinalNode() && node.getChildNodeMap().size() == 0) {
                expandAndEvaluateNode(node);
                treeUpdater.updateTree(node);
            }
            queue.addAll(node.getChildNodeStream().filter(SearchNode::isOpponentTurn).collect(Collectors.toList()));
        }
    }

    @Override
    public boolean updateTree() {
        SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> selectedNodeForExpansion = nodeSelector.selectNextNode();
        if(selectedNodeForExpansion == null) {
            return false;
        }
        if(!selectedNodeForExpansion.isFinalNode()) {
            logger.trace("Selected node [{}] is not final node, expanding", selectedNodeForExpansion);
            expandAndEvaluateNode(selectedNodeForExpansion);
        }
        treeUpdater.updateTree(selectedNodeForExpansion);
        return true;
    }

    protected void checkApplicableAction(TAction action) {
        if(root.isFinalNode()) {
            throw new IllegalStateException("Can't apply action [" + action +"] on final state");
        }
        if(root.isLeaf()) {
//            logger.warn("Trying to apply action [{}] on not expanded node", action);
//            expandAndEvaluateNode(root);
             throw new IllegalStateException("Policy cannot pick action from leaf node");
        }
    }

    protected StateRewardReturn<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> innerApplyAction(TAction action) {
        TReward reward = root.getChildNodeMap().get(action).getSearchNodeMetadata().getGainedReward();
        root = root.getChildNodeMap().get(action);
        root.makeRoot();
        nodeSelector.setNewRoot(root);
        resetTreeStatistics();
        if(!root.isFinalNode()) {
            expandTreeToNextPlayerLevel();
        }
        return new ImmutableStateRewardReturnTuple<>(root.getWrappedState(), reward);
    }

    @Override
    public TAction[] getAllPossibleActions() {
        return this.root.getWrappedState().getAllPossibleActions();
    }

    @Override
    public StateRewardReturn<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> applyAction(TAction action) {
        checkApplicableAction(action);
        return innerApplyAction(action);
    }

    @Override
    public TState deepCopy() {
        throw new UnsupportedOperationException("Deep copy on search tree is not yet defined nad maybe won't be since it's not really needed");
    }

    @Override
    public TPlayerObservation getPlayerObservation() {
        return root.getWrappedState().getPlayerObservation();
    }

    @Override
    public TOpponentObservation getOpponentObservation() {
        return root.getWrappedState().getOpponentObservation();
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
    public SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> getRoot() {
        return root;
    }

    private void expandAndEvaluateNode(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> selectedNodeForExpansion) {
        nodeEvaluator.evaluateNode(selectedNodeForExpansion);
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

    @Override
    public String toString() {
        LinkedList<SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> queue = new LinkedList<>();
        queue.addFirst(this.getRoot());

        StringBuilder string = new StringBuilder();
        String start = "digraph G {";
        String end = "}";

        string.append(start);
        while(!queue.isEmpty()) {
            SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node = queue.poll();
            for (Map.Entry<TAction, SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> entry : node.getChildNodeMap().entrySet()) {
                SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> child = entry.getValue();
                queue.addLast(child);

                string.append("\"" + node.toString() + "\"");
                string.append(" -> ");
                string.append("\"" + child.toString() + "\"");
                string.append(" ");
                string.append("[ label = \"P(");
                string.append(entry.getKey());
                string.append("\" ]; \n");
            }
        }
        string.append(end);
        return string.toString();
    }

    public void printTreeToFile(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> subtreeRoot, String fileName, int depthBound) {
        while (depthBound >= 1) {
            try {
                printTreeToFileInternal(subtreeRoot, fileName, depthBound, a -> true);
                return;
            } catch(GraphvizException e) {
                logger.error("Tree dump with depth [{}] failed. Halving depth to [{}] and trying again", depthBound, depthBound / 2);
            }
            depthBound = depthBound / 2;
        }
    }

    protected void printTreeToFileInternal(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> subtreeRoot,
                                         String fileName,
                                         int depthBound,
                                         Function<SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>, Boolean> filter) {
        var queue = new LinkedList<ImmutableTuple<SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>, Integer>>();
        queue.addFirst(new ImmutableTuple<>(subtreeRoot, 0));

        Graph graph = graph("example1")
            .directed()
            .graphAttr()
            .with(RankDir.TOP_TO_BOTTOM);

        while(!queue.isEmpty()) {
            var nodeParent = queue.poll();
            if(nodeParent.getSecond() < depthBound) {
                for (var child : nodeParent.getFirst().getChildNodeMap().entrySet()) {
                    if(filter.apply(child.getValue())) {
                        queue.addLast(new ImmutableTuple<>(child.getValue(), nodeParent.getSecond() + 1));
                    }
                    graph = graph.with(
                        node(nodeParent.getFirst().toString())
                            .link(
                                to(node(child.getValue().toString()))
                                    .with(Label.of(child.getKey().toString()))
                            )
                    );
                }
            }
        }
        try {
            Graphviz.fromGraph(graph).render(Format.SVG).toFile(new File(fileName + ".svg"));
        } catch (IOException e) {
            throw new IllegalStateException("Saving into graph failed", e);
        }
    }
}
