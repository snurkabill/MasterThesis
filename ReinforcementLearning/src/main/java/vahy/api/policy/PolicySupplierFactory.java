package vahy.api.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

import java.util.SplittableRandom;

public interface PolicySupplierFactory<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>, TPolicyRecord extends PolicyRecord> {

    PolicySupplier<TAction, TObservation, TState, TPolicyRecord> createPolicySupplier(int policyId, int categoryId, SplittableRandom random);

}
