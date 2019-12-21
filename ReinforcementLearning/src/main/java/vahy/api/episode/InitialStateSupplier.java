package vahy.api.episode;

import vahy.api.experiment.ProblemConfig;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

public interface InitialStateSupplier<
    TConfig extends ProblemConfig,
    TAction extends Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    TState createInitialState();
}
