package vahy.api.policy;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.State;

public interface PolicySupplier<
        TAction extends Action,
        TPlayerObservation extends Observation,
        TOpponentObservation extends Observation,
        TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
        TPolicyRecord> {

    Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> initializePolicy(TState initialState, PolicyMode policyMode);

}
