package vahy.impl.experiment;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.api.policy.PolicySupplier;

public class AbstractExperiment<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    public void run() {
//
////        var systemConfig = new SystemConfig();
////        var algorithmConfig = new AlgorithmConfig();
////        var problemConfig = new ProblemConfig();
////        var path = Path.of("asdf");
////        var episodeWriter = new EpisodeWriter<>(problemConfig, algorithmConfig, systemConfig, path);
////
////
////        var stageTrainer_input = new AbstractTrainer<>();
//
//
////
////        AbstractTrainer<> stageTrainer = stageTrainer_input;
//
////        var trainingCycle  = new PolicyTrainingCycle<>(systemConfig, algorithmConfig, episodeWriter, stageTrainer);
////
////        Duration trainingDuration = trainingCycle.executeTrainingPolicy();
//
//
//
//        var policyTrainingCycleList = new ArrayList<PolicyTrainingCycle>();
//
//        policyTrainingCycleList.stream().map(x -> new ImmutableTuple<>())


        PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policySupplierBuilder = null;
        PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> opponentPolicySupplierBuilder = null;
//        var environmentInstanceSupplier =


    }

//    private Trainer<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getAbstractTrainer(
//        PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policySupplier,
//        PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> opponentPolicySupplier,
//        EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeResultsFactory,
//        InitialStateSupplier<TAction, TPlayerObservation, TOpponentObservation, TState>initialStateSupplier,
//        AlgorithmConfig algorithmConfig,
//        SystemConfig systemConfig,
//        ProgressTrackerSettings progressTrackerSettings,
//        List<FromEpisodesDataPointGeneratorGeneric<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> additionalDataPointGeneratorList
//        )
//    {
//
//        var discountFactor = algorithmConfig.getDiscountFactor();
//        var trainerAlgorithm = algorithmConfig.getDataAggregationAlgorithm();
//
//        var gameSampler = new GameSamplerImpl<>(
//            initialStateSupplier,
//            episodeResultsFactory,
//            PolicyMode.TRAINING,
//            progressTrackerSettings,
//            systemConfig.getParallelThreadsCount(),
//            policySupplier,
//            opponentPolicySupplier,
//            additionalDataPointGeneratorList
//            );
//
//        var dataAggregator = trainerAlgorithm.resolveDataAggregator(algorithmConfig);
//        return new PaperTrainer<>(gameSampler, approximator, discountFactor, dataAggregator);
//    }
}
