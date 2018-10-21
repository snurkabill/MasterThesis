package vahy.paper.reinforcement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.paper.policy.EnvironmentPolicySupplier;
import vahy.paper.policy.PaperPolicyImpl;
import vahy.paper.policy.PolicySupplier;
import vahy.paper.reinforcement.episode.PaperEpisode;
import vahy.api.model.StateRewardReturn;
import vahy.environment.state.ImmutableStateImpl;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.impl.model.reward.DoubleScalarReward;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EpisodeAggregator {

    private static final Logger logger = LoggerFactory.getLogger(EpisodeAggregator.class);

    private final int uniqueEpisodeCount;
    private final int episodeIterationCount;
    private final HallwayGameInitialInstanceSupplier initialStateSupplier;
    private final PolicySupplier playerPolicySupplier;
    private final EnvironmentPolicySupplier opponentPolicy;

    public EpisodeAggregator(
        int uniqueEpisodeCount,
        int episodeIterationCount,
        HallwayGameInitialInstanceSupplier initialStateSupplier,
        PolicySupplier playerPolicySupplier,
        EnvironmentPolicySupplier opponentPolicy)
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
                PaperPolicyImpl policy = playerPolicySupplier.initializePolicy(initialGameState);
                PaperEpisode paperEpisode = new PaperEpisode(initialGameState, policy, opponentPolicy.initializePolicy(initialGameState));
                paperEpisode.runEpisode();
                rewardHistory.add(paperEpisode.getEpisodeStateRewardReturnList().stream().map(StateRewardReturn::getReward).collect(Collectors.toList()));
            }
        }
        return rewardHistory;
    }
}
