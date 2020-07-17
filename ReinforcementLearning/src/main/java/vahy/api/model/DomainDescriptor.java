package vahy.api.model;

import vahy.api.model.observation.Observation;

public interface DomainDescriptor<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>> {

    TAction[] getAllEnvironmentActions();

    TAction[] getAllPlayerActions();

    default int getActionLocalIndex(TAction action) {
        return action.getLocalIndex();
    }

    default int getCountOfAllActionsFromSameEntity(TAction action) {
        return action.getCountOfAllActionsFromSameEntity();
    }



}
