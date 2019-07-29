package vahy.paperGenerics.reinforcement.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.PaperPolicy;
import vahy.paperGenerics.policy.PaperPolicySupplier;

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
    TReward extends DoubleReward,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractGameSampler.class.getName());

    private final InitialStateSupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier;
    private final PaperPolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> opponentPolicySupplier;
    private final int stepCountLimit;

    private final ExecutorService executorService;
    private ProgressTracker<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> progressTracker;

    public AbstractGameSampler(InitialStateSupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                               PaperPolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> opponentPolicySupplier,
                               int stepCountLimit) {
        this.initialStateSupplier = initialStateSupplier;
        this.opponentPolicySupplier = opponentPolicySupplier;
        this.stepCountLimit = stepCountLimit;
        var processingUnitCount = Runtime.getRuntime().availableProcessors() - 1;
        logger.info("Initialized [{}] executors for", processingUnitCount);
        this.executorService = Executors.newFixedThreadPool(processingUnitCount);
    }

    public List<EpisodeResults<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> sampleEpisodes(int episodeBatchSize) {
        logger.info("Sampling [{}] episodes started", episodeBatchSize);
        var episodesToSample = new ArrayList<Callable<EpisodeResults<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>>(episodeBatchSize);
        for (int i = 0; i < episodeBatchSize; i++) {
            TState initialGameState = initialStateSupplier.createInitialState();
            var paperPolicy = supplyPlayerPolicy(initialGameState);
            var opponentPolicy = opponentPolicySupplier.initializePolicy(initialGameState);
            var paperEpisode = new EpisodeImmutableSetup<>(initialGameState, paperPolicy, opponentPolicy, stepCountLimit);
            var episodeSimulator = new EpisodeSimulator<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>();
            episodesToSample.add(() -> episodeSimulator.calculateEpisode(paperEpisode));
        }
        try {
            List<Future<EpisodeResults<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>> results = executorService.invokeAll(episodesToSample);
            var paperEpisodeHistoryList = results.stream().map(x -> {
                try {
                    return x.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new IllegalStateException("Parallel episodes were interrupted.", e);
                }
            }).collect(Collectors.toList());

            if(logger.isDebugEnabled()) {
                if(progressTracker == null) {
                     progressTracker = new ProgressTracker<>();
                }
                progressTracker.addData(paperEpisodeHistoryList);
            }
            return paperEpisodeHistoryList;
        } catch (InterruptedException e) {
            throw new IllegalStateException("Parallel episodes were interrupted.", e);
        }
    }

    protected abstract PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> supplyPlayerPolicy(TState initialState);
}
