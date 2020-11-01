package vahy.api.predictor;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

import java.util.List;

public interface PerfectStatePredictor<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TState extends State<TAction, TObservation, TState>> {

    double[] apply(TState observation);

    double[][] apply(TState[] observationArray);

    List<double[]> apply(List<TState> observationList);

}
