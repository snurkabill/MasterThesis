package vahy.paperGenerics.reinforcement.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.PaperPolicy;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.utils.MathStreamUtils;
import vahy.vizualiation.ProgressTracker;
import vahy.vizualiation.ProgressTrackerSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public abstract class AbstractGameSampler<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractGameSampler.class.getName());

    private final InitialStateSupplier<TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier;
    private final PaperPolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> opponentPolicySupplier;
    private final int stepCountLimit;

    private final ProgressTracker progressTracker;
    private final List<FromEpisodesDataPointGenerator<TAction, TPlayerObservation, TOpponentObservation, TState>> dataPointGeneratorList = new ArrayList<>();
    private int batchCounter = 0;
    private final int processingUnitCount;

    public AbstractGameSampler(InitialStateSupplier<TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                               PaperPolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> opponentPolicySupplier,
                               ProgressTrackerSettings progressTrackerSettings,
                               int stepCountLimit,
                               int processingUnitCount) {
        this.initialStateSupplier = initialStateSupplier;
        this.opponentPolicySupplier = opponentPolicySupplier;
        this.progressTracker = new ProgressTracker(progressTrackerSettings);
        this.stepCountLimit = stepCountLimit;
        this.processingUnitCount = processingUnitCount;
        createDataGenerators();
    }

    private void registerDataGenerators() {
        for (FromEpisodesDataPointGenerator fromEpisodesDataPointGenerator : dataPointGeneratorList) {
            progressTracker.registerDataCollector(fromEpisodesDataPointGenerator);
        }
    }

    private void createDataGenerators() {
        dataPointGeneratorList.add(new FromEpisodesDataPointGenerator<>(
            "Avg Player Step Count",
            episodeResults -> MathStreamUtils.calculateAverage(episodeResults, EpisodeResults::getPlayerStepCount)));

        dataPointGeneratorList.add(new FromEpisodesDataPointGenerator<>(
            "Avg Total Payoff",
            episodeResults -> MathStreamUtils.calculateAverage(episodeResults, EpisodeResults::getTotalPayoff)));

        dataPointGeneratorList.add(new FromEpisodesDataPointGenerator<>(
            "Avt risk ratio",
            episodeResults -> MathStreamUtils.calculateAverage(episodeResults, x -> x.isRiskHit() ? 1.0 : 0.0)));

        dataPointGeneratorList.add(new FromEpisodesDataPointGenerator<>(
            "Avg episode duration [ms]",
            episodeResults -> MathStreamUtils.calculateAverage(episodeResults, EpisodeResults::getMillisecondDuration)));

        registerDataGenerators();
    }

    public List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState>> sampleEpisodes(int episodeBatchSize) {
        logger.info("Sampling [{}] episodes started", episodeBatchSize);

        logger.info("Initialized [{}] executors for sampling", processingUnitCount);
        ExecutorService executorService = Executors.newFixedThreadPool(processingUnitCount);

        var episodesToSample = new ArrayList<Callable<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState>>>(episodeBatchSize);
        for (int i = 0; i < episodeBatchSize; i++) {
            TState initialGameState = initialStateSupplier.createInitialState();
            var paperPolicy = supplyPlayerPolicy(initialGameState);
            var opponentPolicy = opponentPolicySupplier.initializePolicy(initialGameState);
            var paperEpisode = new EpisodeImmutableSetup<>(initialGameState, paperPolicy, opponentPolicy, stepCountLimit);
            var episodeSimulator = new EpisodeSimulator<TAction, TPlayerObservation, TOpponentObservation, TState>();
            episodesToSample.add(() -> episodeSimulator.calculateEpisode(paperEpisode));
        }
        try {
            List<Future<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState>>> results = executorService.invokeAll(episodesToSample);
            var paperEpisodeHistoryList = results.stream().map(x -> {
                try {
                    return x.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new IllegalStateException("Parallel episodes were interrupted.", e);
                }
            }).collect(Collectors.toList());

            for (FromEpisodesDataPointGenerator<TAction, TPlayerObservation, TOpponentObservation, TState> fromEpisodesDataPointGenerator : dataPointGeneratorList) {
                fromEpisodesDataPointGenerator.addNewValue(paperEpisodeHistoryList, batchCounter);
            }
            progressTracker.onNextLog();
            batchCounter++;
            executorService.shutdown();
            return paperEpisodeHistoryList;
        } catch (InterruptedException e) {
            throw new IllegalStateException("Parallel episodes were interrupted.", e);
        }
    }

    protected abstract PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> supplyPlayerPolicy(TState initialState);
}
