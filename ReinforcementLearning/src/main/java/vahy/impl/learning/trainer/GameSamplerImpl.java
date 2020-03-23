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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class GameSamplerImpl<
    TConfig extends ProblemConfig,
    TAction extends Enum<TAction> & Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord>
    implements GameSampler<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> {

    private static final Logger logger = LoggerFactory.getLogger(GameSamplerImpl.class.getName());

    private final InitialStateSupplier<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier;
    private final EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> resultsFactory;
    private final int processingUnitCount;

    private final PolicyMode policyMode;

    private final PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> playerPolicySupplier;
    private final PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> opponentPolicySupplier;

    public GameSamplerImpl(
        InitialStateSupplier<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
        EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> resultsFactory,
        PolicyMode policyMode,
        int processingUnitCount,
        PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> playerPolicySupplier,
        PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> opponentPolicySupplier)
    {
        this.initialStateSupplier = initialStateSupplier;
        this.resultsFactory = resultsFactory;
        this.policyMode = policyMode;
        this.processingUnitCount = processingUnitCount;
        this.playerPolicySupplier = playerPolicySupplier;
        this.opponentPolicySupplier = opponentPolicySupplier;
    }

    private Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> supplyPlayerPolicy(TState initialState, PolicyMode policyMode) {
        return playerPolicySupplier.initializePolicy(initialState, policyMode);
    }

    private Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> supplyOpponentPolicy(TState initialState, PolicyMode policyMode) {
        return opponentPolicySupplier.initializePolicy(initialState, policyMode);
    }

    public List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> sampleEpisodes(int episodeBatchSize, int stepCountLimit) {
        ExecutorService executorService = Executors.newFixedThreadPool(processingUnitCount);
        logger.info("Initialized [{}] executors for sampling", processingUnitCount);
        logger.info("Sampling [{}] episodes started", episodeBatchSize);
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

            executorService.shutdown();
            return paperEpisodeHistoryList;
        } catch (InterruptedException e) {
            throw new IllegalStateException("Parallel episodes were interrupted.", e);
        }
    }

}
