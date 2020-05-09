package vahy.api.policy;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.State;

public interface PolicySupplier<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>, TPolicyRecord extends PolicyRecord> {

    Policy<TAction, TObservation, TState, TPolicyRecord> initializePolicy(TState initialState, PolicyMode policyMode);

}
