package vahy.paper.reinforcement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.environment.state.HallwayStateImpl;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.paper.policy.EnvironmentPolicySupplier;
import vahy.paper.policy.PaperPolicyImpl;
import vahy.paper.policy.PaperPolicySupplier;
import vahy.paper.reinforcement.episode.PaperEpisode;

import java.util.ArrayList;
import java.util.List;

public class EpisodeAggregator {

    private static final Logger logger = LoggerFactory.getLogger(EpisodeAggregator.class);

    private final int uniqueEpisodeCount;
    private final int episodeIterationCount;
    private final int stepCountLimit;
    private final HallwayGameInitialInstanceSupplier initialStateSupplier;
    private final PaperPolicySupplier playerPolicySupplier;
    private final EnvironmentPolicySupplier opponentPolicy;

    public EpisodeAggregator(
        int uniqueEpisodeCount,
        int episodeIterationCount,
        int stepCountLimit, HallwayGameInitialInstanceSupplier initialStateSupplier,
        PaperPolicySupplier playerPolicySupplier,
        EnvironmentPolicySupplier opponentPolicy)
    {
        this.uniqueEpisodeCount = uniqueEpisodeCount;
        this.episodeIterationCount = episodeIterationCount;
        this.stepCountLimit = stepCountLimit;
        this.playerPolicySupplier = playerPolicySupplier;
        this.opponentPolicy = opponentPolicy;
        this.initialStateSupplier = initialStateSupplier;
    }

    public List<PaperEpisode> runSimulation() {
        logger.info("Running simulation");
        List<PaperEpisode> episodeList = new ArrayList<>();
        for (int i = 0; i < uniqueEpisodeCount; i++) {
            logger.info("Running {}th unique episode", i);
            for (int j = 0; j < episodeIterationCount; j++) {
                logger.info("Running {}th episode", i * episodeIterationCount + j);
                HallwayStateImpl initialGameState = initialStateSupplier.createInitialState();
                PaperPolicyImpl policy = playerPolicySupplier.initializePolicy(initialGameState);
                PaperEpisode paperEpisode = new PaperEpisode(initialGameState, policy, opponentPolicy.initializePolicy(initialGameState), stepCountLimit);
                paperEpisode.runEpisode();
                episodeList.add(paperEpisode);
            }
        }
        return episodeList;
    }
}
