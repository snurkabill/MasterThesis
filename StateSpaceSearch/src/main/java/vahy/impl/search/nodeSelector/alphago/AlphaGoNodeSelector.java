package vahy.impl.search.nodeSelector.alphago;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.impl.search.node.nodeMetadata.alphago.AlphaGoNodeMetadata;
import vahy.impl.search.node.nodeMetadata.alphago.AlphaGoStateActionMetadata;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class AlphaGoNodeSelector<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TState extends State<TAction, TReward, TObservation>>
    implements NodeSelector<TAction, TReward, TObservation, AlphaGoStateActionMetadata<TReward>, AlphaGoNodeMetadata<TAction, TReward>, TState> {

    private SearchNode<TAction, TReward, TObservation, AlphaGoStateActionMetadata<TReward>, AlphaGoNodeMetadata<TAction, TReward>, TState> root;
    private final double cpuctParameter;

    public AlphaGoNodeSelector(double cpuctParameter) {
        this.cpuctParameter = cpuctParameter;
    }

    @Override
    public void addNode(SearchNode<TAction, TReward, TObservation, AlphaGoStateActionMetadata<TReward>, AlphaGoNodeMetadata<TAction, TReward>, TState> node) {
        if(root == null && node.isRoot()) {
            root = node;
        }
    }

    @Override
    public void addNodes(Collection<SearchNode<TAction, TReward, TObservation,AlphaGoStateActionMetadata<TReward>,  AlphaGoNodeMetadata<TAction, TReward>, TState>> searchNodes) {
    }

    @Override
    public SearchNode<TAction, TReward, TObservation, AlphaGoStateActionMetadata<TReward>, AlphaGoNodeMetadata<TAction, TReward>, TState> selectNextNode() {
        if(root == null) {
            throw new IllegalStateException("Root was not initialized");
        }
        SearchNode<TAction, TReward, TObservation, AlphaGoStateActionMetadata<TReward>, AlphaGoNodeMetadata<TAction, TReward>, TState> node = root;
        while(!node.isLeaf()) {
            AlphaGoNodeMetadata<TAction, TReward> searchNodeMetadata = node.getSearchNodeMetadata();
            int totalNodeVisitCount = searchNodeMetadata.getTotalVisitCounter();
            TAction bestAction = node.getSearchNodeMetadata()
                .getStateActionMetadataMap()
                .entrySet()
                .stream()
                .collect(Collectors
                    .toMap(
                        Map.Entry::getKey,
                        entry -> {
                            AlphaGoStateActionMetadata<TReward> value = entry.getValue();
                            return value.getMeanActionValue() + calculateUValue(value, totalNodeVisitCount);
                        }))
                .entrySet()
                .stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .get()
                .getKey();
            node = node.getChildNodeMap().get(bestAction);
        }
        return node;
    }

    private double calculateUValue(AlphaGoStateActionMetadata<TReward> actionMetadata, int nodeTotalVisitCount) {
        return cpuctParameter * actionMetadata.getPriorProbability() * Math.sqrt(nodeTotalVisitCount) / (1.0 + actionMetadata.getVisitCount());
    }
}
