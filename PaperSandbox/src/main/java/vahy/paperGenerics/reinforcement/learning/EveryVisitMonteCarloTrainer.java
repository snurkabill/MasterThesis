package vahy.paperGenerics.reinforcement.learning;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.RewardAggregator;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.api.predictor.TrainablePredictor;
import vahy.paperGenerics.reinforcement.episode.sampler.PaperRolloutGameSampler;

import java.util.Map;

public class EveryVisitMonteCarloTrainer<
    TAction extends Enum<TAction> & Action,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>>
    extends AbstractMonteCarloTrainer<TAction, TOpponentObservation, TSearchNodeMetadata, TState> {

    public EveryVisitMonteCarloTrainer(PaperRolloutGameSampler<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> gameSampler,
                                       TrainablePredictor<DoubleVector> trainablePredictor,
                                       double discountFactor,
                                       RewardAggregator rewardAggregator) {
        super(gameSampler, trainablePredictor, discountFactor, rewardAggregator);
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
