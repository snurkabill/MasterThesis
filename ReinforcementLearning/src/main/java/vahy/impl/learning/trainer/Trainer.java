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
import vahy.impl.episode.DataPointGeneratorGeneric;
import vahy.impl.model.observation.DoubleVector;
import vahy.vizualiation.ProgressTracker;
import vahy.vizualiation.ProgressTrackerSettings;

import java.awt.*;
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
    private final ProgressTracker progressTracker;
    private final DataPointGeneratorGeneric<Double> oobAvgMsPerEpisode = new DataPointGeneratorGeneric<>("OutOfBox avg ms per episode", x -> x);
    private final DataPointGeneratorGeneric<Double> oobSamplingTime = new DataPointGeneratorGeneric<>("Sampling time [s]", x -> x);
    private final DataPointGeneratorGeneric<Double> trainingSampleCount = new DataPointGeneratorGeneric<>("Training sample count", x -> x);
    private final DataPointGeneratorGeneric<Double> secTraining = new DataPointGeneratorGeneric<>("Training time [s]", x -> x);
    private final DataPointGeneratorGeneric<Double> msTrainingPerSample = new DataPointGeneratorGeneric<>("Training per sample [ms]", x -> x);


    public Trainer(TrainablePredictor trainablePredictor,
                   GameSampler<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> gameSampler,
                   DataAggregator dataAggregator,
                   EpisodeDataMaker<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeDataMaker,
                   ProgressTrackerSettings progressTrackerSettings) {
        this.trainablePredictor = trainablePredictor;
        this.gameSampler = gameSampler;
        this.dataAggregator = dataAggregator;
        this.episodeDataMaker = episodeDataMaker;
        this.progressTracker = new ProgressTracker(progressTrackerSettings, "Training stats", Color.BLUE);
        progressTracker.registerDataCollector(oobAvgMsPerEpisode);
        progressTracker.registerDataCollector(oobSamplingTime);
        progressTracker.registerDataCollector(trainingSampleCount);
        progressTracker.registerDataCollector(secTraining);
        progressTracker.registerDataCollector(msTrainingPerSample);
        progressTracker.finalizeRegistration();
    }

    public List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> trainPolicy(
        int episodeBatchSize,
        int stepCountLimit)
    {
        var start = System.currentTimeMillis();
        var episodes = gameSampler.sampleEpisodes(episodeBatchSize, stepCountLimit);
        var samplingTime = System.currentTimeMillis() - start;
        oobAvgMsPerEpisode.addNewValue(samplingTime/(double) episodeBatchSize);
        oobSamplingTime.addNewValue(samplingTime / 1000.0);

        for (var episode : episodes) {
            var dataSamples = episodeDataMaker.createEpisodeDataSamples(episode);
            dataAggregator.addEpisodeSamples(dataSamples);
        }

        var trainingDataset = dataAggregator.getTrainingDataset();
        var datasetSize = trainingDataset.getFirst().length;

        var startTraining = System.currentTimeMillis();
        trainablePredictor.train(trainingDataset);
        var endTraining = System.currentTimeMillis() - startTraining;

        trainingSampleCount.addNewValue((double)datasetSize);
        secTraining.addNewValue(endTraining / 1000.0);
        msTrainingPerSample.addNewValue(endTraining / (double) datasetSize);

        progressTracker.onNextLog();
        return episodes;
    }

    protected double[] evaluatePolicy(DoubleVector observation) {
        return this.trainablePredictor.apply(observation);
    }

}
