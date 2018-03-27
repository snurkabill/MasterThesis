package vahy.impl.search.nodeSelector;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.utils.ImmutableTuple;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public abstract class AbstractPriorityQueueNodeSelector<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward>,
    TState extends State<TAction, TReward, TObservation>>
    implements NodeSelector<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    protected final Queue<ImmutableTuple<Integer, SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState>>> queue;

    public AbstractPriorityQueueNodeSelector() {
        this.queue = new PriorityQueue<>(Comparator.comparing(ImmutableTuple::getFirst));
    }

    @Override
    public SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> selectNextNode() {
        return queue.poll().getSecond();
    }
}
