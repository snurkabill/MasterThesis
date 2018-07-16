package vahy.impl.model.observation;

import vahy.api.model.Action;
import vahy.api.model.observation.AbstractObservationAggregator;

import java.util.Iterator;
import java.util.LinkedList;

public class VectorialStateHistoryObservationAggregator<TAction extends Action> extends AbstractObservationAggregator<TAction, DoubleVectorialObservation, DoubleVectorialObservationAggregation> {

    private final LinkedList<DoubleVectorialObservation> observationQueue = new LinkedList<>();

    protected VectorialStateHistoryObservationAggregator(int aggregationQueueSize) {
        super(aggregationQueueSize);
    }

    @Override
    public boolean isAggregationReady() {
        return isQueueFull(observationQueue);
    }

    @Override
    public void aggregate(TAction playedAction, DoubleVectorialObservation observation) {
        observationQueue.add(observation);
        reduceQueueSize(observationQueue);
    }

    @Override
    public DoubleVectorialObservationAggregation getAggregation() {
        if(!isAggregationReady()) {
            throw new IllegalStateException("Aggregation is not ready yet");
        }
        int lengthOfObservationVector = observationQueue.getFirst().getObservedVector().length;
        int lengthOfAggregatedObservationVector = lengthOfObservationVector * observationQueue.size();
        double[] representation = new double[lengthOfAggregatedObservationVector];
        Iterator<DoubleVectorialObservation> queueIterator = observationQueue.iterator();
        for (int i = 0; queueIterator.hasNext(); i++) {
            DoubleVectorialObservation next = queueIterator.next();
            System.arraycopy(next.getObservedVector(), 0, representation, i * lengthOfObservationVector, lengthOfObservationVector);
        }
        return new DoubleVectorialObservationAggregation(representation);
    }
}
