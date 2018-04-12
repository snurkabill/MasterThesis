package vahy.impl.search.nodeSelector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.utils.ImmutableTuple;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public abstract class AbstractPriorityQueueNodeSelector<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>,
    TPriority extends Comparable<TPriority>>
    implements NodeSelector<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPriorityQueueNodeSelector.class);
    protected final Queue<ImmutableTuple<TPriority, SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState>>> queue;

    public AbstractPriorityQueueNodeSelector() {
        this.queue = new PriorityQueue<>(Comparator.comparing(ImmutableTuple::getFirst));
    }

    @Override
    public SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> selectNextNode() {
        logger.trace("Selecting next node. Queue size: [{}]", queue.size());
        return queue.poll().getSecond();
    }
}

