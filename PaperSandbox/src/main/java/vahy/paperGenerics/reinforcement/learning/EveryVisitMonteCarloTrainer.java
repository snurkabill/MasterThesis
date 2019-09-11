package vahy.paperGenerics.reinforcement.learning;

import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.nodeEvaluator.TrainableNodeEvaluator;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.policy.TrainablePaperPolicySupplier;
import vahy.vizualiation.ProgressTrackerSettings;

import java.util.Map;

public class EveryVisitMonteCarloTrainer<
    TAction extends Enum<TAction> & Action,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>>
    extends AbstractMonteCarloTrainer<TAction, TOpponentObservation, TSearchNodeMetadata, TState> {

    public EveryVisitMonteCarloTrainer(InitialStateSupplier<TAction, DoubleVector, TOpponentObservation, TState> initialStateSupplier,
                                       TrainablePaperPolicySupplier<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> paperTrainablePolicySupplier,
                                       PaperPolicySupplier<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> opponentPolicySupplier,
                                       TrainableNodeEvaluator<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> paperNodeEvaluator,
                                       double discountFactor,
                                       RewardAggregator rewardAggregator,
                                       ProgressTrackerSettings progressTrackerSettings,
                                       int stepCountLimit,
                                       int threadCount) {
        super(initialStateSupplier, paperTrainablePolicySupplier, opponentPolicySupplier, paperNodeEvaluator, discountFactor, rewardAggregator, progressTrackerSettings, stepCountLimit, threadCount);
    }

    @Override
    protected void putDataSample(Map<DoubleVector, MutableDataSample> everyVisitSet, MutableDataSample dataSample, DoubleVector observation) {
        if(!everyVisitSet.containsKey(observation)) {
            everyVisitSet.put(observation, dataSample);
        } else {
            everyVisitSet.get(observation).addDataSample(dataSample);
        }
    }

}
