package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;

public class RegisteredPolicy<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TState extends State<TAction, TObservation, TState>> {

    private final Policy<TAction, TObservation, TState> policy;
    private final int inGameEntityId;
    private final int observationLookbackSize;

    public RegisteredPolicy(Policy<TAction, TObservation, TState> policy, int inGameEntityId, int observationLookbackSize) {
        this.policy = policy;
        this.inGameEntityId = inGameEntityId;
        this.observationLookbackSize = observationLookbackSize;
    }

    public Policy<TAction, TObservation, TState> getPolicy() {
        return policy;
    }

    public int getInGameEntityId() {
        return inGameEntityId;
    }

    public int getPolicyId() {
        return policy.getPolicyId();
    }

    public int getObservationLookbackSize() {
        return observationLookbackSize;
    }
}
