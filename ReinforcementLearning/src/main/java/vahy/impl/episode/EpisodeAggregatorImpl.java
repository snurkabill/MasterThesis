package vahy.impl.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.Episode;
import vahy.api.episode.EpisodeAggregator;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.Reward;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicySupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EpisodeAggregatorImpl<
    TAction extends Action,
    TReward extends Reward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    implements EpisodeAggregator<TReward> {

    private static final Logger logger = LoggerFactory.getLogger(EpisodeAggregator.class);

    private final int uniqueEpisodeCount;
    private final int episodeIterationCount;
    private final InitialStateSupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier;
    private final PolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> playerPolicySupplier;
    private final Policy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> opponentPolicy;

    public EpisodeAggregatorImpl(
        int uniqueEpisodeCount,
        int episodeIterationCount,
        InitialStateSupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
        PolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> playerPolicySupplier,
        Policy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> opponentPolicy)
    {
        this.uniqueEpisodeCount = uniqueEpisodeCount;
        this.episodeIterationCount = episodeIterationCount;
        this.playerPolicySupplier = playerPolicySupplier;
        this.opponentPolicy = opponentPolicy;
        this.initialStateSupplier = initialStateSupplier;
    }

    public List<List<TReward>> runSimulation(){
        logger.info("Running simulation");
        List<List<TReward>> rewardHistory = new ArrayList<>();
        for (int i = 0; i < uniqueEpisodeCount; i++) {
            logger.info("Running {}th unique episode", i);
            for (int j = 0; j < episodeIterationCount; j++) {
                TState initialGameState = initialStateSupplier.createInitialState();
                Policy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> policy = playerPolicySupplier.initializePolicy(initialGameState);
                Episode<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> episode = new EpisodeImpl<>(initialGameState, policy, opponentPolicy);
                episode.runEpisode();
                rewardHistory.add(episode.getEpisodeStateRewardReturnList().stream().map(StateRewardReturn::getReward).collect(Collectors.toList()));
            }
        }
        return rewardHistory;
    }
}
