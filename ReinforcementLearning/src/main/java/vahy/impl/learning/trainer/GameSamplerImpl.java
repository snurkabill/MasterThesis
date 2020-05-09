package vahy.impl.learning.trainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.GameSampler;
import vahy.api.episode.InitialStateSupplier;
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
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>,
    TPolicyRecord extends PolicyRecord>
    implements GameSampler<TAction, TObservation, TState, TPolicyRecord> {

    private static final Logger logger = LoggerFactory.getLogger(GameSamplerImpl.class.getName());

    private final InitialStateSupplier<TAction, TObservation, TState> initialStateSupplier;
    private final EpisodeResultsFactory<TAction, TObservation, TState, TPolicyRecord> resultsFactory;
    private final int processingUnitCount;

    private final List<PolicySupplier<TAction, TObservation, TState, TPolicyRecord>> policySupplierList;

    public GameSamplerImpl(InitialStateSupplier<TAction, TObservation, TState> initialStateSupplier,
                           EpisodeResultsFactory<TAction, TObservation, TState, TPolicyRecord> resultsFactory,
                           List<PolicySupplier<TAction, TObservation, TState, TPolicyRecord>> playerPolicySupplierList,
                           int processingUnitCount)
    {
        this.initialStateSupplier = initialStateSupplier;
        this.resultsFactory = resultsFactory;
        this.processingUnitCount = processingUnitCount;
        this.policySupplierList = playerPolicySupplierList;
    }

    private Policy<TAction, TObservation, TState, TPolicyRecord> supplyPlayerPolicy(PolicySupplier<TAction, TObservation, TState, TPolicyRecord> policySupplier, TState initialState, PolicyMode policyMode) {
        return policySupplier.initializePolicy(initialState, policyMode);
    }

    public List<EpisodeResults<TAction, TObservation, TState, TPolicyRecord>> sampleEpisodes(int episodeBatchSize, int stepCountLimit, PolicyMode policyMode) {
        ExecutorService executorService = Executors.newFixedThreadPool(processingUnitCount);
        logger.info("Initialized [{}] executors for sampling", processingUnitCount);
        logger.info("Sampling [{}] episodes started", episodeBatchSize);
        var episodesToSample = new ArrayList<Callable<EpisodeResults<TAction, TObservation, TState, TPolicyRecord>>>(episodeBatchSize);
        for (int i = 0; i < episodeBatchSize; i++) {
            TState initialGameState = initialStateSupplier.createInitialState(policyMode);
            var policyList = policySupplierList.stream().map(x -> supplyPlayerPolicy(x, initialGameState, policyMode)).collect(Collectors.toList());
            var paperEpisode = new EpisodeSetupImpl<>(initialGameState, policyList, stepCountLimit);
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
