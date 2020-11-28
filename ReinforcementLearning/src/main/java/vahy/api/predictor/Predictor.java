package vahy.api.predictor;

import vahy.api.model.observation.Observation;

import java.util.List;

public interface Predictor<TObservation extends Observation<TObservation>> {

    double[] apply(TObservation observation);

    double[][] apply(TObservation[] observationArray);

    List<double[]> apply(List<TObservation> observationList);

}
