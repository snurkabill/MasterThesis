package vahy.impl.learning.trainer;

import vahy.api.episode.EpisodeResults;
import vahy.api.episode.GameSampler;
import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.api.learning.trainer.EpisodeDataMaker;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.api.predictor.TrainablePredictor;
import vahy.impl.model.observation.DoubleVector;

import java.util.List;

public class Trainer<
    TAction extends Enum<TAction> & Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    private final TrainablePredictor trainablePredictor;
    private final GameSampler<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> gameSampler;
    private final DataAggregator dataAggregator;
    private final EpisodeDataMaker<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeDataMaker;

    public Trainer(TrainablePredictor trainablePredictor,
                   GameSampler<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> gameSampler,
                   DataAggregator dataAggregator,
                   EpisodeDataMaker<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeDataMaker) {
        this.trainablePredictor = trainablePredictor;
        this.gameSampler = gameSampler;
        this.dataAggregator = dataAggregator;
        this.episodeDataMaker = episodeDataMaker;
    }

    public List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> trainPolicy(
        int episodeBatchSize,
        int stepCountLimit)
    {
        var episodes = gameSampler.sampleEpisodes(episodeBatchSize, stepCountLimit);
        for (var episode : episodes) {
            var dataSamples = episodeDataMaker.createEpisodeDataSamples(episode);
            dataAggregator.addEpisodeSamples(dataSamples);
        }
        trainablePredictor.train(dataAggregator.getTrainingDataset());
        return episodes;
    }

    protected double[] evaluatePolicy(DoubleVector observation) {
        return this.trainablePredictor.apply(observation);
    }

}
