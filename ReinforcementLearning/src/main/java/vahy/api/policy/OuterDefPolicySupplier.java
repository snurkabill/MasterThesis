package vahy.api.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;

import java.util.SplittableRandom;

@FunctionalInterface
public interface OuterDefPolicySupplier<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>> {

    Policy<TAction, TObservation, TState> apply(StateWrapper<TAction, TObservation, TState> initialState, PolicyMode policyMode, int policyId, SplittableRandom random);

}
