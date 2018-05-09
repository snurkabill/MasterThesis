package vahy.impl.search.nodeSelector.treeTraversing;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1SearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1StateActionMetadata;
import vahy.utils.StreamUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.SplittableRandom;

public class UCB1NodeSelector<
    TAction extends Action,
    TReward extends DoubleScalarReward,
    TObservation extends Observation,
    TState extends State<TAction, TReward, TObservation>>
    implements NodeSelector<TAction, TReward, TObservation, Ucb1StateActionMetadata<TReward>, Ucb1SearchNodeMetadata<TAction, TReward>, TState> {

    private SearchNode<TAction, TReward, TObservation, Ucb1StateActionMetadata<TReward>, Ucb1SearchNodeMetadata<TAction, TReward>, TState> root;
    private final SplittableRandom random;
    private final double weight;

    public UCB1NodeSelector(SplittableRandom random, double weight) {
        this.random = random;
        this.weight = weight;
    }

    @Override
    public void addNode(SearchNode<TAction, TReward, TObservation, Ucb1StateActionMetadata<TReward>, Ucb1SearchNodeMetadata<TAction, TReward>, TState> node) {

    }

    @Override
    public void addNodes(Collection<SearchNode<TAction, TReward, TObservation, Ucb1StateActionMetadata<TReward>, Ucb1SearchNodeMetadata<TAction, TReward>, TState>> searchNodes) {

    }

    @Override
    public void setNewRoot(SearchNode<TAction, TReward, TObservation, Ucb1StateActionMetadata<TReward>, Ucb1SearchNodeMetadata<TAction, TReward>, TState> root) {
        this.root = root;
    }

    @Override
    public SearchNode<TAction, TReward, TObservation, Ucb1StateActionMetadata<TReward>, Ucb1SearchNodeMetadata<TAction, TReward>, TState> selectNextNode() {
        if(root == null) {
            throw new IllegalStateException("Root was not initialized");
        }
        SearchNode<TAction, TReward, TObservation, Ucb1StateActionMetadata<TReward>, Ucb1SearchNodeMetadata<TAction, TReward>, TState> node = root;
        while(!node.isLeaf()) {
            Ucb1SearchNodeMetadata<TAction, TReward> nodeMetadata =  node.getSearchNodeMetadata();
            nodeMetadata.increaseVisitCounter();
            TAction bestAction = node.getSearchNodeMetadata()
                .getStateActionMetadataMap()
                .entrySet()
                .stream()
                .collect(StreamUtils.toRandomizedMaxCollector(Comparator.comparing(o -> calculateUCBValue(nodeMetadata, o.getValue())), random))
                .getKey();
            nodeMetadata.getStateActionMetadataMap().get(bestAction).increaseVisitCounter();
            node = node.getChildNodeMap().get(bestAction);
        }
        return node;
    }

    private double calculateUCBValue(Ucb1SearchNodeMetadata<TAction, TReward> searchNodeMetadata, Ucb1StateActionMetadata<TReward> actionMetadata) {
        return searchNodeMetadata.getEstimatedTotalReward().getValue() + weight * Math.sqrt(Math.log(actionMetadata.getVisitCounter()) / searchNodeMetadata.getVisitCounter());
    }
}
