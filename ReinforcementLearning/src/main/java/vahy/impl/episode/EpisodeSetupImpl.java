package vahy.impl.episode;

import vahy.api.episode.EpisodeSetup;
import vahy.api.episode.PolicyIdTranslationMap;
import vahy.api.episode.RegisteredPolicy;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

import java.util.List;

public class EpisodeSetupImpl<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>>
    implements EpisodeSetup<TAction, TObservation, TState> {

    private final TState initialState;
    private final PolicyIdTranslationMap policyIdTranslationMap;
    private final List<RegisteredPolicy<TAction, TObservation, TState>> registeredPolicyList;
    private final int stepCountLimit;

    public EpisodeSetupImpl(TState initialState,
                            PolicyIdTranslationMap policyIdTranslationMap,
                            List<RegisteredPolicy<TAction, TObservation, TState>> registeredPolicyList,
                            int stepCountLimit) {
        checkIfSorted(registeredPolicyList);
        this.initialState = initialState;
        this.policyIdTranslationMap = policyIdTranslationMap;
        this.registeredPolicyList = registeredPolicyList;
        this.stepCountLimit = stepCountLimit;
    }

    private void checkIfSorted(List<RegisteredPolicy<TAction, TObservation, TState>> registeredPolicyList) {
        for (int i = 0; i < registeredPolicyList.size(); i++) {
            if(i != registeredPolicyList.get(i).getPolicyId()) {
                throw new IllegalStateException("Registered policies are in wrong order.");
            }
        }
    }

    public TState getInitialState() {
        return initialState;
    }

    @Override
    public PolicyIdTranslationMap getPolicyIdTranslationMap() {
        return policyIdTranslationMap;
    }

    @Override
    public List<RegisteredPolicy<TAction, TObservation, TState>> getRegisteredPolicyList() {
        return registeredPolicyList;
    }

    public int getStepCountLimit() {
        return stepCountLimit;
    }
}
