package vahy.impl.learning.trainer;

import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.benchmark.EpisodeStatisticsCalculator;
import vahy.api.episode.EpisodeResults;
import vahy.api.episode.GameSampler;
import vahy.api.experiment.ProblemConfig;
import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.api.learning.trainer.EpisodeDataMaker;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.api.predictor.TrainablePredictor;
import vahy.impl.episode.DataPointGeneratorGeneric;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;
import vahy.vizualiation.ProgressTracker;
import vahy.vizualiation.ProgressTrackerSettings;

import java.awt.Color;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Trainer<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord,
    TStatistics extends EpisodeStatistics> {

    private final ProblemConfig problemConfig;
    private final TrainablePredictor trainablePredictor;
    private final GameSampler<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> gameSampler;
    private final DataAggregator dataAggregator;
    private final EpisodeDataMaker<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeDataMaker;
    private final ProgressTracker trainingProgressTracker;
    private final ProgressTracker samplingProgressTracker;
    private final EpisodeStatisticsCalculator<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord, TStatistics> statisticsCalculator;

    private final List<DataPointGeneratorGeneric<TStatistics>> dataPointGeneratorList = new ArrayList<>();

    private final DataPointGeneratorGeneric<Double> oobAvgMsPerEpisode = new DataPointGeneratorGeneric<>("OutOfBox avg ms per episode", x -> x);
    private final DataPointGeneratorGeneric<Double> oobSamplingTime = new DataPointGeneratorGeneric<>("Sampling time [s]", x -> x);
    private final DataPointGeneratorGeneric<Double> trainingSampleCount = new DataPointGeneratorGeneric<>("Training sample count", x -> x);
    private final DataPointGeneratorGeneric<Double> secTraining = new DataPointGeneratorGeneric<>("Training time [s]", x -> x);
    private final DataPointGeneratorGeneric<Double> msTrainingPerSample = new DataPointGeneratorGeneric<>("Training per sample [ms]", x -> x);

    public Trainer(TrainablePredictor trainablePredictor,
                   GameSampler<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> gameSampler,
                   DataAggregator dataAggregator,
                   EpisodeDataMaker<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeDataMaker,
                   ProgressTrackerSettings progressTrackerSettings,
                   ProblemConfig problemConfig,
                   EpisodeStatisticsCalculator<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord, TStatistics> statisticsCalculator,
                   List<DataPointGeneratorGeneric<TStatistics>> additionalDataPointGeneratorList) {
        this.problemConfig = problemConfig;
        this.trainablePredictor = trainablePredictor;
        this.gameSampler = gameSampler;
        this.dataAggregator = dataAggregator;
        this.episodeDataMaker = episodeDataMaker;
        this.statisticsCalculator = statisticsCalculator;
        this.trainingProgressTracker = new ProgressTracker(progressTrackerSettings, "Training stats", Color.BLUE);
        trainingProgressTracker.registerDataCollector(oobAvgMsPerEpisode);
        trainingProgressTracker.registerDataCollector(oobSamplingTime);
        trainingProgressTracker.registerDataCollector(trainingSampleCount);
        trainingProgressTracker.registerDataCollector(secTraining);
        trainingProgressTracker.registerDataCollector(msTrainingPerSample);
        trainingProgressTracker.finalizeRegistration();

        this.samplingProgressTracker =  new ProgressTracker(progressTrackerSettings, "Sampling stats", Color.RED);
        createSamplingDataGenerators(additionalDataPointGeneratorList);
        samplingProgressTracker.finalizeRegistration();
    }

    private void createSamplingDataGenerators(List<DataPointGeneratorGeneric<TStatistics>> additionalDataPointGeneratorList) {
        var dataPointGeneratorList = new ArrayList<>(additionalDataPointGeneratorList == null ? new ArrayList<>() : additionalDataPointGeneratorList);

        dataPointGeneratorList.add(new DataPointGeneratorGeneric<>("Avg Player Step Count", EpisodeStatistics::getAveragePlayerStepCount));

        dataPointGeneratorList.add(new DataPointGeneratorGeneric<>("Avg Total Payoff", EpisodeStatistics::getTotalPayoffAverage));

        dataPointGeneratorList.add(new DataPointGeneratorGeneric<>("Stdev Total Payoff", EpisodeStatistics::getTotalPayoffStdev));

        dataPointGeneratorList.add(new DataPointGeneratorGeneric<>("Avg episode duration [ms]", EpisodeStatistics::getAverageMillisPerEpisode));

        dataPointGeneratorList.add(new DataPointGeneratorGeneric<>("Stdev episode duration [ms]", EpisodeStatistics::getStdevMillisPerEpisode));

        registerDataGenerators(dataPointGeneratorList);
    }

    protected void registerDataGenerators(List<DataPointGeneratorGeneric<TStatistics>> dataPointGeneratorList) {
        this.dataPointGeneratorList.addAll(dataPointGeneratorList);
        for (var fromEpisodesDataPointGenerator : dataPointGeneratorList) {
            samplingProgressTracker.registerDataCollector(fromEpisodesDataPointGenerator);
        }
    }

    public ImmutableTuple<List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>>, TStatistics> trainPolicy(int episodeBatchSize) {
        var start = System.currentTimeMillis();
        var episodes = gameSampler.sampleEpisodes(episodeBatchSize, problemConfig.getMaximalStepCountBound());
        var samplingTime = System.currentTimeMillis() - start;

        var stats = statisticsCalculator.calculateStatistics(episodes, Duration.ofMillis(samplingTime - start));

        for (var fromEpisodesDataPointGenerator : dataPointGeneratorList) {
            fromEpisodesDataPointGenerator.addNewValue(stats);
        }

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

        trainingProgressTracker.onNextLog();
        samplingProgressTracker.onNextLog();
        return new ImmutableTuple<>(episodes, stats);
    }

    protected double[] evaluatePolicy(DoubleVector observation) {
        return this.trainablePredictor.apply(observation);
    }

}
