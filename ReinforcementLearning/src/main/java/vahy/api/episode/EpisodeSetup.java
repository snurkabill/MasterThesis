package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;

import java.util.List;

public interface EpisodeSetup<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>, TPolicyRecord extends PolicyRecord> {

    TState getInitialState();

    PolicyIdTranslationMap getPolicyIdTranslationMap();

    List<RegisteredPolicy<TAction, TObservation, TState, TPolicyRecord>> getRegisteredPolicyList();

    int getStepCountLimit();
}
