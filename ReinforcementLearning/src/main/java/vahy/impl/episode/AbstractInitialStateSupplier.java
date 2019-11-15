package vahy.impl.episode;

import vahy.api.episode.InitialStateSupplier;
import vahy.api.experiment.ProblemConfig;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

import java.util.SplittableRandom;

public abstract class AbstractInitialStateSupplier<
    TConfig extends ProblemConfig,
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements InitialStateSupplier<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState> {

    private final TConfig problemConfig;
    private final SplittableRandom random;

    protected AbstractInitialStateSupplier(TConfig problemConfig, SplittableRandom random) {
        this.problemConfig = problemConfig;
        this.random = random;
    }

    @Override
    public TState createInitialState() {
        return createState_inner(problemConfig, random);
    }

    protected abstract TState createState_inner(TConfig problemConfig, SplittableRandom random);
}
