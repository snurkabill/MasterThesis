package vahy.paperGenerics.experiment;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;

public abstract class Experiment<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    private final ExperimentSetup experimentSetup;

    protected Experiment(ExperimentSetup experimentSetup) {
        this.experimentSetup = experimentSetup;
    }

    public ExperimentSetup getExperimentSetup() {
        return experimentSetup;
    }

    public PaperPolicyResults<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> runExperiment() {





        return null;

    }

//
//    private PaperNodeEvaluator<TAction, TOpponentObservation, TSearchNodeMetadata, TState> resolveEvaluator(EvaluatorType evaluatorType,
//                                                                                                            SplittableRandom random,
//                                                                                                            ExperimentSetup experimentSetup,
//                                                                                                            DoubleScalarRewardAggregator rewardAggregator,
//                                                                                                            SearchNodeBaseFactoryImpl<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeFactory,
//                                                                                                            TrainableApproximator<DoubleVector> approximator) {
//        switch (evaluatorType) {
//            case MONTE_CARLO:
//                return new MonteCarloNodeEvaluator<>(
//                    searchNodeFactory,
//                    EnvironmentProbabilities::getProbabilities,
//                    HallwayAction.playerActions,
//                    HallwayAction.environmentActions,
//                    random.split(),
//                    rewardAggregator,
//                    experimentSetup.getDiscountFactor());
//            case RALF:
//                return new PaperNodeEvaluator<>(
//                    searchNodeFactory,
//                    approximator,
//                    EnvironmentProbabilities::getProbabilities,
//                    HallwayAction.playerActions,
//                    HallwayAction.environmentActions);
//            case RAMCP:
//                return new RamcpNodeEvaluator<>(
//                    searchNodeFactory,
//                    EnvironmentProbabilities::getProbabilities,
//                    HallwayAction.playerActions,
//                    HallwayAction.environmentActions,
//                    random.split(),
//                    rewardAggregator,
//                    experimentSetup.getDiscountFactor());
//            default:
//                throw EnumUtils.createExceptionForUnknownEnumValue(evaluatorType);
//        }
//    }

}
