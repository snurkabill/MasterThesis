package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.State;

public interface InitialStateSupplier<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    TState createInitialState();
}
