package vahy.impl.model.observation;

import vahy.api.model.observation.Observation;

public class DoubleVectorialObservation implements Observation {

    private final double[] observedVector;

    public DoubleVectorialObservation(double[] observedVector) {
        this.observedVector = observedVector;
    }

    public double[] getObservedVector() {
        return observedVector;
    }
}
