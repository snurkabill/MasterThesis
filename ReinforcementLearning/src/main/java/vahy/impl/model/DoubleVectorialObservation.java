package vahy.impl.model;

import vahy.api.model.Observation;

public class DoubleVectorialObservation implements Observation {

    private final double[] observedVector;

    public DoubleVectorialObservation(double[] observedVector) {
        this.observedVector = observedVector;
    }

    public double[] getObservedVector() {
        return observedVector;
    }
}
