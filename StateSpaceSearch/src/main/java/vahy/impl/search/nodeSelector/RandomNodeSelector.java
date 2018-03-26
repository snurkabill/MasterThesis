package vahy.impl.search.nodeSelector;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.nodeSelector.NodeSelector;

import java.util.Collection;
import java.util.LinkedList;
import java.util.SplittableRandom;

public class RandomNodeSelector<
        TAction extends Action,
        TReward extends Reward,
        TObservation extends Observation,
        TSearchNodeMetadata extends SearchNodeMetadata,
        TState extends State<TAction, TReward, TObservation>>
        implements NodeSelector<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    private final SplittableRandom random;
    private final LinkedList<SearchNode<TAction, TReward, TObservation, TState, TSearchNodeMetadata>> nodeList;

    public RandomNodeSelector(SplittableRandom random) {
        this.random = random;
        this.nodeList = new LinkedList<>(); // using linked implementation here // TODO: generalize
    }

    @Override
    public void addNode(SearchNode<TAction, TReward, TObservation, TState, TSearchNodeMetadata> rootNode) {
        nodeList.add(rootNode);
    }

    @Override
    public void addNodes(Collection<SearchNode<TAction, TReward, TObservation, TState, TSearchNodeMetadata>> rootNodes) {
        nodeList.addAll(rootNodes);
    }

    @Override
    public SearchNode<TAction, TReward, TObservation, TState, TSearchNodeMetadata> selectNextNode() {
        return nodeList.remove(random.nextInt(nodeList.size()));
    }
}
