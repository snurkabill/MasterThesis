package vahy.api.model.observation;

import vahy.api.model.Action;

public interface ObservationAggregator<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TObservationAggregation extends ObservationAggregation> {

    void aggregate(TAction playedAction, TObservation observation);

    TObservationAggregation getAggregation();

}
