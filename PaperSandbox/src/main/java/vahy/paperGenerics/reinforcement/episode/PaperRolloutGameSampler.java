package vahy.paperGenerics.reinforcement.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.environment.state.PaperState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.policy.PaperPolicy;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.policy.TrainablePaperPolicySupplier;

import java.util.ArrayList;
import java.util.List;

public class PaperRolloutGameSampler<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TObservation extends DoubleVector,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TObservation, TState>> {

    private static final Logger logger = LoggerFactory.getLogger(PaperRolloutGameSampler.class.getName());

    private final InitialStateSupplier<TAction, TReward, TObservation, TState> initialStateSupplier;
    private final TrainablePaperPolicySupplier<TAction, TReward, TObservation, TSearchNodeMetadata, TState> playerPolicySupplier;
    private final PaperPolicySupplier<TAction, TReward, TObservation, TSearchNodeMetadata, TState> opponentPolicySupplier;
    private final int stepCountLimit;
    private final EpisodeSimulator<TAction, TReward, TObservation, TState> episodeSimulator = new EpisodeSimulator<>();

    public PaperRolloutGameSampler(InitialStateSupplier<TAction, TReward, TObservation, TState> initialStateSupplier,
                                   TrainablePaperPolicySupplier<TAction, TReward, TObservation, TSearchNodeMetadata, TState> playerPolicySupplier,
                                   PaperPolicySupplier<TAction, TReward, TObservation, TSearchNodeMetadata, TState> opponentPolicySupplier,
                                   int stepCountLimit) {
        this.initialStateSupplier = initialStateSupplier;
        this.playerPolicySupplier = playerPolicySupplier;
        this.opponentPolicySupplier = opponentPolicySupplier;
        this.stepCountLimit = stepCountLimit;
    }

    public List<EpisodeResults<TAction, TReward, TObservation, TState>> sampleEpisodes(int episodeBatchSize) {
        List<EpisodeResults<TAction, TReward, TObservation, TState>> paperEpisodeHistoryList = new ArrayList<>();
        logger.info("Sampling [{}] episodes started", episodeBatchSize);
        for (int j = 0; j < episodeBatchSize; j++) {
            logger.info("Running [{}]th paperEpisode", j);
            TState initialGameState = initialStateSupplier.createInitialState();
            PaperPolicy<TAction, TReward, TObservation, TState> paperPolicy = playerPolicySupplier.initializePolicyWithExploration(initialGameState);
            PaperPolicy<TAction, TReward, TObservation, TState> opponentPolicy = opponentPolicySupplier.initializePolicy(initialGameState);
            EpisodeImmutableSetup<TAction, TReward, TObservation, TState> paperEpisode = new EpisodeImmutableSetup<>(initialGameState, paperPolicy, opponentPolicy, stepCountLimit);
            paperEpisodeHistoryList.add(episodeSimulator.calculateEpisode(paperEpisode));
        }
        return paperEpisodeHistoryList;
    }

}
