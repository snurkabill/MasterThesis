package vahy.api.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;

public interface Policy<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>> {

    int getPolicyId();

    TAction getDiscreteAction(StateWrapper<TAction, TObservation, TState> gameState);

    void updateStateOnPlayedAction(TAction action);

    PolicyRecord getPolicyRecord(StateWrapper<TAction, TObservation, TState> gameState);

}
