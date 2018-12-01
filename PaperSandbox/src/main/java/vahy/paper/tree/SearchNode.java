package vahy.paper.tree;

import com.quantego.clp.CLPVariable;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.RankDir;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import vahy.environment.HallwayAction;
import vahy.environment.state.HallwayStateImpl;
import vahy.impl.model.reward.DoubleReward;
import vahy.utils.ImmutableTuple;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Link.to;

public class SearchNode {

    private static long instanceCounter = 0;

    private final long nodeId = instanceCounter++;

    private final HallwayStateImpl wrappedState;

    private SearchNode parent;
    private HallwayAction appliedParentAction;
    private DoubleReward gainedReward;
    private DoubleReward cumulativeReward;
    private double realRisk;
    private CLPVariable nodeProbabilityFlow;

    private int totalVisitCounter;  // sum over all b : N(s, b)
    private DoubleReward estimatedReward; // in article V value
    private double estimatedRisk; // in article V value

    private boolean isFakeRisk = false; // only for that weird MC algorithm

    private final Map<HallwayAction, SearchNode> childMap = new HashMap<>();
    private final Map<HallwayAction, EdgeMetadata> edgeMetadataMap = new HashMap<>();

    private boolean alreadyEvaluated = false;

    public SearchNode(HallwayStateImpl wrappedState, SearchNode parent, HallwayAction appliedParentAction, DoubleReward gainedReward) {
        this.wrappedState = wrappedState;
        this.parent = parent;
        this.appliedParentAction = appliedParentAction;
        this.gainedReward = gainedReward;
        if(parent != null) {
            this.cumulativeReward = new DoubleReward(parent.getCumulativeReward().getValue() + gainedReward.getValue());
        } else {
            this.cumulativeReward = new DoubleReward(gainedReward.getValue());
        }
        this.realRisk = wrappedState.isAgentKilled() ? 1 : 0;
    }

    public CLPVariable getNodeProbabilityFlow() {
        return nodeProbabilityFlow;
    }

    public void setNodeProbabilityFlow(CLPVariable nodeProbabilityFlow) {
        this.nodeProbabilityFlow = nodeProbabilityFlow;
    }

    public boolean isAlreadyEvaluated() {
        return alreadyEvaluated;
    }

    public void setEvaluated() {
        alreadyEvaluated = true;
    }

    public void setEstimatedReward(DoubleReward estimatedReward) {
        this.estimatedReward = estimatedReward;
    }

    public DoubleReward getGainedReward() {
        return gainedReward;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public HallwayStateImpl getWrappedState() {
        return wrappedState;
    }

    public SearchNode getParent() {
        return parent;
    }

    public HallwayAction getAppliedParentAction() {
        return appliedParentAction;
    }

    public int getTotalVisitCounter() {
        return totalVisitCounter;
    }

    public DoubleReward getEstimatedReward() {
        return estimatedReward;
    }

    public boolean isLeaf() {
        return isFinalNode() || childMap.entrySet().stream().noneMatch(x -> x.getValue().isAlreadyEvaluated());
    }

    public Map<HallwayAction, EdgeMetadata> getEdgeMetadataMap() {
        return edgeMetadataMap;
    }

    public boolean isFinalNode() {
        return wrappedState.isFinalState();
    }

    public void makeRoot() {
        parent = null;
        appliedParentAction = null;
    }

    public boolean isOpponentTurn() {
        return wrappedState.isOpponentTurn();
    }

    public boolean isAgentTurn() {
        return wrappedState.isAgentTurn();
    }

    public void setTotalVisitCounter(int totalVisitCounter) {
        this.totalVisitCounter = totalVisitCounter;
    }

    public DoubleReward getCumulativeReward() {
        return cumulativeReward;
    }

    public String toStringForGraphwiz() {
        DecimalFormat df = new DecimalFormat("#.####");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ID: ");
        stringBuilder.append(nodeId);
        stringBuilder.append("\\nTotalVisit: ");
        stringBuilder.append(this.totalVisitCounter);
        stringBuilder.append("\\nCumulativeRew: ");
        stringBuilder.append(this.cumulativeReward.getValue());
        stringBuilder.append("\\nEstimatedRew: ");
        stringBuilder.append(estimatedReward != null ? df.format(estimatedReward.getValue()) : null);
        stringBuilder.append("\\nEstimatedRisk: ");
        stringBuilder.append(df.format(getEstimatedRisk()));
        if(parent != null) {
            stringBuilder.append("\\nMeanRew: ");
            stringBuilder.append(df.format(parent.getEdgeMetadataMap().get(appliedParentAction).getMeanActionValue()));
            stringBuilder.append("\\nMeanRisk: ");
            stringBuilder.append(df.format(parent.getEdgeMetadataMap().get(appliedParentAction).getMeanRiskValue()));
        }
        stringBuilder.append("\\nisLeaf: ");
        stringBuilder.append(isLeaf());
        stringBuilder.append("\\nisFakeRisk: ");
        stringBuilder.append(isFakeRisk);
        stringBuilder.append("\\nisEvaluated: ");
        stringBuilder.append(isAlreadyEvaluated());
        stringBuilder.append("\\nNodeProbabilityFlow: ");
        stringBuilder.append(nodeProbabilityFlow != null ? df.format(nodeProbabilityFlow.getSolution()) : null);
        return stringBuilder.toString();
    }

    public String toStringAsRootForGraphwiz() {
        DecimalFormat df = new DecimalFormat("#.####");
        LinkedList<SearchNode> queue = new LinkedList<>();
        queue.addFirst(this);

        StringBuilder string = new StringBuilder();
        String start = "digraph G {";
        String end = "}";

        string.append(start);
        while(!queue.isEmpty()) {
            SearchNode node = queue.poll();

            for (Map.Entry<HallwayAction, EdgeMetadata> entry : node.getEdgeMetadataMap().entrySet()) {
                SearchNode child = node.getChildMap().get(entry.getKey());
                queue.addLast(child);

                string.append("\"" + node.toStringForGraphwiz() + "\"");
                string.append(" -> ");
                string.append("\"" + child.toStringForGraphwiz() + "\"");
                string.append(" ");
                string.append("[ label = \"P(");
                string.append(entry.getKey());
                string.append(") = ");
                string.append(df.format(entry.getValue().getPriorProbability()));
                string.append("\" ]; \n");
            }
        }
        string.append(end);
        return string.toString();
    }

    public void printTreeToFile(String fileName, int depthBound) {

        DecimalFormat df = new DecimalFormat("#.####");
        LinkedList<ImmutableTuple<SearchNode, Integer>> queue = new LinkedList<>();
        queue.addFirst(new ImmutableTuple<>(this, 0));

        Graph graph = graph("example1")
            .directed()
            .graphAttr()
            .with(RankDir.TOP_TO_BOTTOM);

        while(!queue.isEmpty()) {
            ImmutableTuple<SearchNode, Integer> node = queue.poll();
            if(node.getSecond() < depthBound) {
                for (Map.Entry<HallwayAction, EdgeMetadata> entry : node.getFirst().getEdgeMetadataMap().entrySet()) {
                    SearchNode child = node.getFirst().getChildMap().get(entry.getKey());
                    queue.addLast(new ImmutableTuple<>(child, node.getSecond() + 1));
                    graph = graph.with(
                        node(node.getFirst().toStringForGraphwiz())
                            .link(
                                to(node(child.toStringForGraphwiz()))
                                    .with(Label.of("P(" + entry.getKey() + ") = " + df.format(entry.getValue().getPriorProbability())))
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

    public void setEstimatedRisk(double estimatedRisk) {
        this.estimatedRisk = estimatedRisk;
    }

    public double getEstimatedRisk() {
        return estimatedRisk;
    }

    public Map<HallwayAction, SearchNode> getChildMap() {
        return childMap;
    }

    public double getRealRisk() {
        return realRisk;
    }

    public boolean isFakeRisk() {
        return isFakeRisk;
    }

    public void setFakeRisk(boolean fakeRisk) {
        isFakeRisk = fakeRisk;
    }
}
