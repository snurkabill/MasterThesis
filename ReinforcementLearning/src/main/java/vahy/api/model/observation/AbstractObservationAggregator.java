package vahy.api.model.observation;

import vahy.api.model.Action;

import java.util.Queue;

public abstract class AbstractObservationAggregator<
    TAction extends Action<TAction>,
    TObservation extends Observation,
    TObservationAggregation extends ObservationAggregation>
    implements ObservationAggregator<TAction, TObservation, TObservationAggregation> {

    private final int aggregationQueueSize;

    protected AbstractObservationAggregator(int aggregationQueueSize) {
        this.aggregationQueueSize = aggregationQueueSize;
    }

    protected <T> boolean isQueueFull(Queue<T> queue) {
        if(queue.size() > aggregationQueueSize) {
            throw new IllegalStateException("Queue size [{" + queue.size() + "}] should not exceed aggregationQueueSize [{" + aggregationQueueSize + "}]");
        }
        return queue.size() == aggregationQueueSize;
    }

    protected <T> void reduceQueueSize(Queue<T> queue) {
        if(queue.size() > aggregationQueueSize) {
            while(queue.size() != aggregationQueueSize) {
                queue.remove();
            }
        }
    }

    public abstract boolean isAggregationReady();
}
