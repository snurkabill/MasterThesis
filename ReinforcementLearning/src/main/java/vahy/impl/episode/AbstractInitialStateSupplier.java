package vahy.impl.episode;

import vahy.api.episode.InitialStateSupplier;
import vahy.api.experiment.ProblemConfig;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyMode;

import java.util.SplittableRandom;

public abstract class AbstractInitialStateSupplier<
    TConfig extends ProblemConfig,
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>>
    implements InitialStateSupplier<TAction, TObservation, TState> {

    private final TConfig problemConfig;
    private final SplittableRandom random;

    protected AbstractInitialStateSupplier(TConfig problemConfig, SplittableRandom random) {
        this.problemConfig = problemConfig;
        this.random = random;
    }

    @Override
    public TState createInitialState(PolicyMode policyMode) {
        return createState_inner(problemConfig, random.split(), policyMode);
    }

    protected abstract TState createState_inner(TConfig problemConfig, SplittableRandom random, PolicyMode policyMode);
}
