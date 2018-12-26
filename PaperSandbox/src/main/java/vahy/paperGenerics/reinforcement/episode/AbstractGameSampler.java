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
    private final EpisodeSimulator<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> episodeSimulator = new EpisodeSimulator<>();

    public AbstractGameSampler(InitialStateSupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                               PaperPolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> opponentPolicySupplier,
                               int stepCountLimit) {
        this.initialStateSupplier = initialStateSupplier;
        this.opponentPolicySupplier = opponentPolicySupplier;
        this.stepCountLimit = stepCountLimit;
    }

    public List<EpisodeResults<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> sampleEpisodes(int episodeBatchSize) {
        List<EpisodeResults<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> paperEpisodeHistoryList = new ArrayList<>();
        logger.info("Sampling [{}] episodes started", episodeBatchSize);
        for (int j = 0; j < episodeBatchSize; j++) {
            TState initialGameState = initialStateSupplier.createInitialState();
            PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> paperPolicy = supplyPlayerPolicy(initialGameState);
            PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> opponentPolicy = opponentPolicySupplier.initializePolicy(initialGameState);
            EpisodeImmutableSetup<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> paperEpisode = new EpisodeImmutableSetup<>(initialGameState, paperPolicy, opponentPolicy, stepCountLimit);
            EpisodeResults<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> episodeResult = episodeSimulator.calculateEpisode(paperEpisode);
            paperEpisodeHistoryList.add(episodeResult);
            logger.info("Episode [{}] finished. Total steps done: [{}]. Is risk hit: [{}]", j, episodeResult.getEpisodeHistoryList().size(), episodeResult.isRiskHit());
        }
        return paperEpisodeHistoryList;
    }

    protected abstract PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> supplyPlayerPolicy(TState initialState);
}
