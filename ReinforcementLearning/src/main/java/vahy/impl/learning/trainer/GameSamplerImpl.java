package vahy.impl.learning.trainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.GameSampler;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.experiment.ProblemConfig;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecord;
import vahy.api.policy.PolicySupplier;
import vahy.impl.episode.EpisodeSetupImpl;
import vahy.impl.episode.EpisodeSimulatorImpl;
import vahy.impl.episode.FromEpisodesDataPointGeneratorGeneric;
import vahy.utils.MathStreamUtils;
import vahy.vizualiation.ProgressTracker;
import vahy.vizualiation.ProgressTrackerSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class GameSamplerImpl<
    TConfig extends ProblemConfig,
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord>
    implements GameSampler<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> {

    private static final Logger logger = LoggerFactory.getLogger(GameSamplerImpl.class.getName());

    private final InitialStateSupplier<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier;
    private final EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> resultsFactory;
    private final int processingUnitCount;

    private final ProgressTracker progressTracker;
    private final List<FromEpisodesDataPointGeneratorGeneric<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> dataPointGeneratorList = new ArrayList<>();

    private int batchCounter = 0;
    private final PolicyMode policyMode;

    private final PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> playerPolicySupplier;
    private final PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> opponentPolicySupplier;

    public GameSamplerImpl(
        InitialStateSupplier<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
        EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> resultsFactory,
        PolicyMode policyMode,
        ProgressTrackerSettings progressTrackerSettings,
        int processingUnitCount,
        PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> playerPolicySupplier,
        PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> opponentPolicySupplier,
        List<FromEpisodesDataPointGeneratorGeneric<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> additionalDataPointGeneratorList)
    {
        this.initialStateSupplier = initialStateSupplier;
        this.resultsFactory = resultsFactory;
        this.policyMode = policyMode;
        this.processingUnitCount = processingUnitCount;
        this.progressTracker = new ProgressTracker(progressTrackerSettings);
        this.playerPolicySupplier = playerPolicySupplier;
        this.opponentPolicySupplier = opponentPolicySupplier;
        createDataGenerators(additionalDataPointGeneratorList);
    }

    private void createDataGenerators(List<FromEpisodesDataPointGeneratorGeneric<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> additionalDataPointGeneratorList) {
        var dataPointGeneratorList = new ArrayList<>(additionalDataPointGeneratorList == null ? new ArrayList<>() : additionalDataPointGeneratorList);

        dataPointGeneratorList.add(new FromEpisodesDataPointGeneratorGeneric<>(
            "Avg Player Step Count",
            episodeResults -> MathStreamUtils.calculateAverage(episodeResults, EpisodeResults::getPlayerStepCount)));

        dataPointGeneratorList.add(new FromEpisodesDataPointGeneratorGeneric<>(
            "Avg Total Payoff",
            episodeResults -> MathStreamUtils.calculateAverage(episodeResults, EpisodeResults::getTotalPayoff)));

        dataPointGeneratorList.add(new FromEpisodesDataPointGeneratorGeneric<>(
            "Avg episode duration [ms]",
            episodeResults -> MathStreamUtils.calculateAverage(episodeResults, (x) -> x.getDuration().toMillis())));

        registerDataGenerators(dataPointGeneratorList);
    }

    protected void registerDataGenerators(List<FromEpisodesDataPointGeneratorGeneric<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> dataPointGeneratorList) {
        this.dataPointGeneratorList.addAll(dataPointGeneratorList);
        for (var fromEpisodesDataPointGenerator : dataPointGeneratorList) {
            progressTracker.registerDataCollector(fromEpisodesDataPointGenerator);
        }
    }

    private Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> supplyPlayerPolicy(TState initialState, PolicyMode policyMode) {
        return playerPolicySupplier.initializePolicy(initialState, policyMode);
    }

    private Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> supplyOpponentPolicy(TState initialState, PolicyMode policyMode) {
        return opponentPolicySupplier.initializePolicy(initialState, policyMode);
    }

    public List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> sampleEpisodes(int episodeBatchSize, int stepCountLimit) {
        logger.info("Sampling [{}] episodes started", episodeBatchSize);

        logger.info("Initialized [{}] executors for sampling", processingUnitCount);
        ExecutorService executorService = Executors.newFixedThreadPool(processingUnitCount);

        var episodesToSample = new ArrayList<Callable<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>>>(episodeBatchSize);
        for (int i = 0; i < episodeBatchSize; i++) {
            TState initialGameState = initialStateSupplier.createInitialState();
            var paperPolicy = supplyPlayerPolicy(initialGameState, policyMode);
            var opponentPolicy = supplyOpponentPolicy(initialGameState, policyMode);
            var paperEpisode = new EpisodeSetupImpl<>(initialGameState, paperPolicy, opponentPolicy, stepCountLimit);
            var episodeSimulator = new EpisodeSimulatorImpl<>(resultsFactory);
            episodesToSample.add(() -> episodeSimulator.calculateEpisode(paperEpisode));
        }
        try {
            var results = executorService.invokeAll(episodesToSample);
            var paperEpisodeHistoryList = results.stream().map(x -> {
                try {
                    return x.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new IllegalStateException("Parallel episodes were interrupted.", e);
                }
            }).collect(Collectors.toList());

            for (var fromEpisodesDataPointGenerator : dataPointGeneratorList) {
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


}
