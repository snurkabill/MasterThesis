package vahy.impl.episode;

import vahy.api.episode.EpisodeSetup;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyRecord;

import java.util.List;

public class EpisodeSetupImpl<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>, TPolicyRecord extends PolicyRecord>
    implements EpisodeSetup<TAction, TObservation, TState, TPolicyRecord> {

    private final TState initialState;
    private final List<Policy<TAction, TObservation, TState, TPolicyRecord>> policyList;
    private final int stepCountLimit;

    public EpisodeSetupImpl(TState initialState, List<Policy<TAction, TObservation, TState, TPolicyRecord>> policyList, int stepCountLimit) {
        this.initialState = initialState;
        this.policyList = policyList;
        this.stepCountLimit = stepCountLimit;
    }

    public TState getInitialState() {
        return initialState;
    }

    @Override
    public List<Policy<TAction, TObservation, TState, TPolicyRecord>> getPolicyList() {
        return policyList;
    }

    public int getStepCountLimit() {
        return stepCountLimit;
    }
}
