package vahy.AlphaGo.reinforcement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.AlphaGo.policy.AlphaGoEnvironmentPolicySupplier;
import vahy.AlphaGo.policy.AlphaGoPolicyImpl;
import vahy.AlphaGo.policy.AlphaGoPolicySupplier;
import vahy.AlphaGo.reinforcement.episode.AlphaGoEpisode;
import vahy.api.model.StateRewardReturn;
import vahy.environment.state.ImmutableStateImpl;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.impl.model.reward.DoubleScalarReward;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AlphaGoEpisodeAggregator {

    private static final Logger logger = LoggerFactory.getLogger(AlphaGoEpisodeAggregator.class);

    private final int uniqueEpisodeCount;
    private final int episodeIterationCount;
    private final HallwayGameInitialInstanceSupplier initialStateSupplier;
    private final AlphaGoPolicySupplier playerPolicySupplier;
    private final AlphaGoEnvironmentPolicySupplier opponentPolicy;

    public AlphaGoEpisodeAggregator(
        int uniqueEpisodeCount,
        int episodeIterationCount,
        HallwayGameInitialInstanceSupplier initialStateSupplier,
        AlphaGoPolicySupplier playerPolicySupplier,
        AlphaGoEnvironmentPolicySupplier opponentPolicy)
    {
        this.uniqueEpisodeCount = uniqueEpisodeCount;
        this.episodeIterationCount = episodeIterationCount;
        this.playerPolicySupplier = playerPolicySupplier;
        this.opponentPolicy = opponentPolicy;
        this.initialStateSupplier = initialStateSupplier;
    }

    public List<List<DoubleScalarReward>> runSimulation(){
        logger.info("Running simulation");
        List<List<DoubleScalarReward>> rewardHistory = new ArrayList<>();
        for (int i = 0; i < uniqueEpisodeCount; i++) {
            logger.info("Running {}th unique episode", i);
            for (int j = 0; j < episodeIterationCount; j++) {
                ImmutableStateImpl initialGameState = initialStateSupplier.createInitialState();
                AlphaGoPolicyImpl policy = playerPolicySupplier.initializePolicy(initialGameState);
                AlphaGoEpisode episode = new AlphaGoEpisode(initialGameState, policy, opponentPolicy.initializePolicy(initialGameState));
                episode.runEpisode();
                rewardHistory.add(episode.getEpisodeStateRewardReturnList().stream().map(StateRewardReturn::getReward).collect(Collectors.toList()));
            }
        }
        return rewardHistory;
    }
}
