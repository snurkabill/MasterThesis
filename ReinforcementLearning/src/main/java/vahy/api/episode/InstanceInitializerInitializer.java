package vahy.api.episode;

import vahy.api.experiment.ProblemConfig;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

import java.util.SplittableRandom;

public interface InstanceInitializerInitializer<
    TConfig extends ProblemConfig,
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    // this is WTF class.

    InitialStateSupplier<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState> createInitialStateSupplier(TConfig problemConfig, SplittableRandom random);

}
