package vahy.api.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

import java.util.SplittableRandom;

public interface PolicySupplierFactory<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TState extends State<TAction, TObservation, TState>> {

    PolicySupplier<TAction, TObservation, TState> createPolicySupplier(int policyId, int categoryId, SplittableRandom random);

}
