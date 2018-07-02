package vahy.impl.search.nodeSelector.treeTraversing;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.utils.StreamUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.SplittableRandom;

public class EGreedyNodeSelector<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>>
    implements NodeSelector<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {

    private SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> root;
    private final double epsilon;
    private final SplittableRandom random;

    public EGreedyNodeSelector(double epsilon, SplittableRandom random) {
        this.epsilon = epsilon;
        this.random = random;
    }

    @Override
    public void addNode(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> node) {
    }

    @Override
    public void addNodes(Collection<SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState>> searchNodes) {
    }

    @Override
    public void setNewRoot(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> root) {
        this.root = root;
    }

    @Override
    public SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> selectNextNode() {
        if(root == null) {
            throw new IllegalStateException("Root was not initialized");
        }
        SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> node = root;
        while(!node.isLeaf()) {
            if(random.nextDouble() < epsilon) {
                node = node.getChildNodeMap().entrySet().stream().skip(random.nextInt(node.getChildNodeMap().size())).findFirst().get().getValue();
            } else {
                TAction bestAction = node.getSearchNodeMetadata()
                    .getStateActionMetadataMap()
                    .entrySet()
                    .stream()
                    .collect(StreamUtils.toRandomizedMaxCollector(Comparator.comparing(o -> o.getValue().getEstimatedTotalReward()), random))
                    .getKey();
                node = node.getChildNodeMap().get(bestAction);
            }
        }
        return node;
    }
}
