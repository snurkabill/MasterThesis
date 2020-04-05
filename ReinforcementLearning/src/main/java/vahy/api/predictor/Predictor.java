package vahy.api.predictor;

import vahy.api.model.observation.Observation;

public interface Predictor<TObservation extends Observation> {

    double[] apply(TObservation observation);

    double[][] apply(TObservation[] observationArray);

}
