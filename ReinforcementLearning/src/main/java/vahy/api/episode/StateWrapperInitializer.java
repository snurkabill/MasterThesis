package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;

public interface StateWrapperInitializer<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TState extends State<TAction, TObservation, TState>> {

    StateWrapper<TAction, TObservation, TState> createInitialStateWrapper(int inGameEntityId, int policyObservationLookbackSize, TState state);

}
