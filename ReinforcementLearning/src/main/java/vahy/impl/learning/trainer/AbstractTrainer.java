package vahy.impl.learning.trainer;

import vahy.api.episode.EpisodeResults;
import vahy.api.episode.GameSampler;
import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.api.predictor.TrainablePredictor;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.List;

public abstract class AbstractTrainer<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    private final TrainablePredictor trainablePredictor;
    private final GameSampler<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> gameSampler;
    private final DataAggregator dataAggregator;

    protected AbstractTrainer(TrainablePredictor trainablePredictor,
                              GameSampler<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> gameSampler,
                              DataAggregator dataAggregator) {
        this.trainablePredictor = trainablePredictor;
        this.gameSampler = gameSampler;
        this.dataAggregator = dataAggregator;
    }

    protected abstract List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples(
        EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeResults);

    public void trainPolicy(int episodeBatchSize, int stepCountLimit) {
        var episodes = gameSampler.sampleEpisodes(episodeBatchSize, stepCountLimit);
        for (var episode : episodes) {
            var dataSamples = createEpisodeDataSamples(episode);
            dataAggregator.addEpisodeSamples(dataSamples);
        }
        trainablePredictor.train(dataAggregator.getTrainingDataset());
    }

    protected double[] evaluatePolicy(DoubleVector observation) {
        return this.trainablePredictor.apply(observation);
    }

}
