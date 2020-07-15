package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;

public class RegisteredPolicy<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>> {

    private final Policy<TAction, TObservation, TState> policy;
    private final int inGameEntityId;

    public RegisteredPolicy(Policy<TAction, TObservation, TState> policy, int inGameEntityId) {
        this.policy = policy;
        this.inGameEntityId = inGameEntityId;
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
}
