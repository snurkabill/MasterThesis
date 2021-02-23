package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

import java.util.List;

public interface EpisodeSetup<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TState extends State<TAction, TObservation, TState>> {

    TState getInitialState();

    PolicyIdTranslationMap getPolicyIdTranslationMap();

    List<RegisteredPolicy<TAction, TObservation, TState>> getRegisteredPolicyList();

    int getStepCountLimit();

    int getEpisodeId();
}
