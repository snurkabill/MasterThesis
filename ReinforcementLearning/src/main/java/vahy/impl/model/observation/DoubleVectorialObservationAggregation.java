package vahy.impl.model.observation;

import vahy.api.model.observation.ObservationAggregation;

public class DoubleVectorialObservationAggregation implements ObservationAggregation {

    private final double[] representation;

    public DoubleVectorialObservationAggregation(double[] representation) {
        this.representation = representation;
    }

    public double[] getRepresentation() {
        return representation;
    }
}
