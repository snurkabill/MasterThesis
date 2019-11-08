package vahy.paperGenerics;

import vahy.api.experiment.AlgorithmConfig;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.impl.experiment.AbstractExperiment;

import java.lang.reflect.InvocationTargetException;

public class PaperExperimentEntryPoint {

    public static <
        TAction extends Enum<TAction> & Action,
        TPlayerObservation extends Observation,
        TOpponentObservation extends Observation,
        TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
        TPolicyRecord extends PolicyRecord> void createExperimentAndRun(Class<TAction> actionClass,
                                                                        Class<TPlayerObservation> playerObservationClass,
                                                                        Class<TOpponentObservation> opponentObservationClass,
                                                                        Class<TState> stateClass,
                                                                        Class<TPolicyRecord> policyRecordClass,
                                                                        Class initialInstanceSupplierClass,
                                                                        AlgorithmConfig algorithmConfig,
                                                                        SystemConfig systemConfig,
                                                                        ProblemConfig problemConfig) {

        AbstractExperiment<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> experiment = createExperimentInstance();




    }


    private static <
        TAction extends Enum<TAction> & Action,
        TPlayerObservation extends Observation,
        TOpponentObservation extends Observation,
        TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
        TPolicyRecord extends PolicyRecord>
    AbstractExperiment<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> createExperimentInstance() {
        try {
            return AbstractExperiment.class.getConstructor().newInstance(null);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Unable to create instance of " + AbstractExperiment.class.getName(), e);
        }
    }


}
