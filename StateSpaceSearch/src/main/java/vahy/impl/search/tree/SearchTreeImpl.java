package vahy.impl.search.tree;


import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.RankDir;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizException;
import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import guru.nidi.graphviz.model.Graph;
import static guru.nidi.graphviz.model.Link.to;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.NodeMetadata;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.api.search.tree.SearchTree;
import vahy.api.search.update.TreeUpdater;
import vahy.utils.ImmutableTuple;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SearchTreeImpl<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends NodeMetadata,
    TState extends State<TAction, TObservation, TState>>
    implements SearchTree<TAction, TObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(SearchTreeImpl.class);
    public static final boolean TRACE_ENABLED = logger.isTraceEnabled();
    public static final boolean DEBUG_ENABLED = logger.isDebugEnabled() || TRACE_ENABLED;

    private final SearchNodeFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeFactory;
    private SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> root;

    private final NodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> nodeSelector;
    private final NodeEvaluator<TAction, TObservation, TSearchNodeMetadata, TState> nodeEvaluator;
    private final TreeUpdater<TAction, TObservation, TSearchNodeMetadata, TState> treeUpdater;

    private int totalNodesExpanded = 0;

    public SearchTreeImpl(SearchNodeFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeFactory,
                          SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> root,
                          NodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> nodeSelector,
                          TreeUpdater<TAction, TObservation, TSearchNodeMetadata, TState> treeUpdater,
                          NodeEvaluator<TAction, TObservation, TSearchNodeMetadata, TState> nodeEvaluator) {
        this.searchNodeFactory = searchNodeFactory;
        this.root = root;
        this.nodeSelector = nodeSelector;
        this.treeUpdater = treeUpdater;
        this.nodeEvaluator = nodeEvaluator;

//        expandTreeToNextPlayerLevel();
    }

    @Override
    public boolean expandTree() {
        if(root.isFinalNode()) {
            return false;
        }
        SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectedNodeForExpansion = nodeSelector.selectNextNode(this.root);
        if(selectedNodeForExpansion == null) {
            return false;
        }
        if(!selectedNodeForExpansion.isFinalNode()) {
            if(TRACE_ENABLED) {
                logger.trace("Selected node [{}] is not final node, expanding", selectedNodeForExpansion);
            }
            expandAndEvaluateNode(selectedNodeForExpansion);
        }
        treeUpdater.updateTree(selectedNodeForExpansion);
        return true;
    }

    public void applyAction(TAction action) {
        checkApplicableAction(action);
        innerApplyAction(action);
    }

    public int getTotalNodesExpanded() {
        return totalNodesExpanded;
    }

    @Override
    public SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return dumpTreeToString(Integer.MAX_VALUE);
    }

    public String toStringWithBoundedDepth(int depth) {
        return dumpTreeToString(depth);
    }

    private String dumpTreeToString(int depth) {
        LinkedList<ImmutableTuple<SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>, Integer>> queue = new LinkedList<>();
        queue.addFirst(new ImmutableTuple<>(this.getRoot(), 0));

        StringBuilder string = new StringBuilder();
        String start = "digraph G {";
        String end = "}";

        string.append(start);
        while(!queue.isEmpty()) {
            var node = queue.poll();
            for (var entry : node.getFirst().getChildNodeMap().entrySet()) {
                var child = entry.getValue();
                if(node.getSecond() < depth) {
                    queue.addLast(new ImmutableTuple<>(child, node.getSecond() + 1));
                }

                string.append("\"").append(node.getFirst().toString()).append("\"");
                string.append(" -> ");
                string.append("\"").append(child.toString()).append("\"");
                string.append(" ");
                string.append("[ label = \"");
                string.append(entry.getKey());
                string.append("\"];");
                string.append(System.lineSeparator());
            }
        }
        string.append(end);
        return string.toString();
    }

    protected void checkApplicableAction(TAction action) {
        if(root.isFinalNode()) {
            throw new IllegalStateException("Can't apply action [" + action +"] on final state");
        }
//        if(root.isLeaf()) {
//            throw new IllegalStateException("Policy cannot apply action to leaf node without expanded descendants");
//        }
//        if(!root.getChildNodeMap().containsKey(action)) {
//            throw new IllegalStateException("Action [" + action + "] is invalid and cannot be applied to current policy state");
//        }
    }

    protected void innerApplyAction(TAction action) {
        if(!root.getChildNodeMap().containsKey(action)) {
            var stateRewardReturn = root.applyAction(action);
            root = searchNodeFactory.createNode(stateRewardReturn, null, action);
        } else {
            root = root.getChildNodeMap().get(action);
            root.makeRoot();
        }
    }

    protected void expandTreeToNextPlayerLevel() {
        if(root.isFinalNode()) {
            throw new IllegalArgumentException("Cannot expand final node");
        }
        if(root.isLeaf()) {
            if(TRACE_ENABLED) {
                logger.debug("Expanding root since it is not final node and has no children expanded");
            }
            expandAndEvaluateNode(root);
            treeUpdater.updateTree(root);
        }
        var queue = root.getChildNodeStream().filter(SearchNode::isOpponentTurn).collect(Collectors.toCollection(LinkedList::new));
        while(!queue.isEmpty()) {
            var node = queue.pop();
            if(node.isLeaf() && !node.isFinalNode()) {
                expandAndEvaluateNode(node);
            }
            treeUpdater.updateTree(node);
//            queue.addAll(node.getChildNodeStream().filter(SearchNode::isOpponentTurn).collect(Collectors.toList()));
            node.getChildNodeStream().filter(SearchNode::isOpponentTurn).forEach(queue::add);
        }
    }

    public void printTreeToFile(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> subtreeRoot, String fileName, int depthBound) {
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

    protected void printTreeToFileInternal(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> subtreeRoot,
                                           String fileName,
                                           int depthBound,
                                           Function<SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>, Boolean> filter) {
        var queue = new LinkedList<ImmutableTuple<SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>, Integer>>();
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

    private void expandAndEvaluateNode(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectedNodeForExpansion) {
        totalNodesExpanded += nodeEvaluator.evaluateNode(selectedNodeForExpansion);
    }

}
