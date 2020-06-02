package vahy.api.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;

public interface PolicySupplier<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>, TPolicyRecord extends PolicyRecord> {

    int getPolicyId();

    int getPolicyCategoryId();

    Policy<TAction, TObservation, TState, TPolicyRecord> initializePolicy(StateWrapper<TAction, TObservation, TState> initialState, PolicyMode policyMode);

}
