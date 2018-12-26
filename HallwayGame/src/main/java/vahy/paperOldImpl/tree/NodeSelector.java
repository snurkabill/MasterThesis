package vahy.paperOldImpl.tree;

import vahy.environment.HallwayAction;
import vahy.utils.ImmutableTuple;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.Map;
import java.util.SplittableRandom;

public class NodeSelector {

    public static final double TOLERANCE = 0.0000000001;
    private SearchNode root;
    private final double cpuctParameter;
    private final SplittableRandom random;

    public NodeSelector(double cpuctParameter, SplittableRandom random) {
        this.cpuctParameter = cpuctParameter;
        this.random = random;
    }

    public void setNewRoot(SearchNode root) {
        this.root = root;
    }

    public void addNode(SearchNode node) {
        if(root == null && node.isRoot()) {
            root = node;
        }
    }

    public SearchNode selectNextNode() {
        if(root == null) {
            throw new IllegalStateException("Root was not initialized");
        }
        SearchNode node = root;
        while(!node.isLeaf()) {
            int totalNodeVisitCount = node.getTotalVisitCounter();

            double max = node.getEdgeMetadataMap()
                .entrySet()
                .stream()
                .mapToDouble(x -> x.getValue().getMeanActionValue())
              //   .map(Math::abs)
                .max().orElseThrow(() -> new IllegalStateException("Maximum Does not exists"));

            double min = node.getEdgeMetadataMap()
                .entrySet()
                .stream()
                .mapToDouble(x -> x.getValue().getMeanActionValue())
                // .map(Math::abs)
                .min().orElseThrow(() -> new IllegalStateException("Minimum Does not exists"));


            final double finalMax = max;
            final double finalMin = min;

            HallwayAction bestAction = node.getEdgeMetadataMap()
                .entrySet()
                .stream()
                .map(x -> {
                    HallwayAction action = x.getKey();
                    double uValue = calculateUValue(x.getValue().getPriorProbability(), x.getValue().getVisitCount(), totalNodeVisitCount);
                    double qValue = x.getValue().getMeanActionValue() == 0 ? 0 :
                        (x.getValue().getMeanActionValue() - finalMin) /
                            (Math.abs(finalMax - finalMin) < TOLERANCE ? finalMax : (finalMax - finalMin));

                    return new ImmutableTuple<>(action, qValue + uValue);
                })
                .collect(StreamUtils.toRandomizedMaxCollector(Comparator.comparing(ImmutableTuple::getSecond), random))
                .getFirst();



            double[] meanActionValues = new double[node.getEdgeMetadataMap().size()];
            double[] uValues = new double[node.getEdgeMetadataMap().size()];
            HallwayAction[] actions = new HallwayAction[node.getEdgeMetadataMap().size()];


            int i = 0;
            for (Map.Entry<HallwayAction, EdgeMetadata> entry : node.getEdgeMetadataMap().entrySet()) {
                actions[i] = entry.getKey();
                meanActionValues[i] = (entry.getValue().getMeanActionValue() - finalMin) / (Math.abs(finalMax - finalMin) < 0.00000001 ? finalMax : (finalMax - finalMin));
                uValues[i] = calculateUValue(entry.getValue().getPriorProbability(), entry.getValue().getVisitCount(), totalNodeVisitCount);
                i++;
            }


            double maxBound = Double.NEGATIVE_INFINITY;
            int bestIndex = -1;
            for (int j = 0; j < meanActionValues.length; j++) {
                if(maxBound < uValues[j] + meanActionValues[j]) {
                    maxBound = uValues[j] + meanActionValues[j];
                    bestIndex = j;
                }
            }

            node = node.getChildMap().get(bestAction);
        }
        return node;
    }

    private double calculateUValue(double priorProbability, int childVisitCount, int nodeTotalVisitCount) {
        return cpuctParameter * priorProbability * Math.sqrt(nodeTotalVisitCount) / (1.0 + childVisitCount);
    }
}
