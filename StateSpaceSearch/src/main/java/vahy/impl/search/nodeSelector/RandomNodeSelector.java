package vahy.impl.search.nodeSelector;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.utils.ImmutableTuple;

import java.util.Collection;
import java.util.SplittableRandom;

public class RandomNodeSelector<
        TAction extends Action,
        TReward extends Reward,
        TObservation extends Observation,
        TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward>,
        TState extends State<TAction, TReward, TObservation>>
        extends AbstractPriorityQueueNodeSelector<TAction,TReward,TObservation,TSearchNodeMetadata,TState> {

    private final SplittableRandom random;

    public RandomNodeSelector(SplittableRandom random) {
        super();
        this.random = random;
    }

    @Override
    public void addNode(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> rootNode) {
        queue.add(new ImmutableTuple<>(random.nextInt(), rootNode));
    }

    @Override
    public void addNodes(Collection<SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> rootNodes) {
        rootNodes.forEach(this::addNode);
    }


}
