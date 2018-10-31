package vahy.paper.reinforcement.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.paper.policy.EnvironmentPolicySupplier;
import vahy.paper.policy.PaperPolicy;
import vahy.paper.policy.PaperTrainablePaperPolicySupplier;
import vahy.environment.agent.policy.environment.EnvironmentPolicy;
import vahy.environment.state.ImmutableStateImpl;
import vahy.game.HallwayGameInitialInstanceSupplier;

import java.util.ArrayList;
import java.util.List;

public class PaperRolloutGameSampler {

    private static final Logger logger = LoggerFactory.getLogger(PaperRolloutGameSampler.class.getName());

    private final HallwayGameInitialInstanceSupplier initialStateSupplier;
    private final PaperTrainablePaperPolicySupplier playerPolicySupplier;
    private final EnvironmentPolicySupplier opponentPolicySupplier;

    public PaperRolloutGameSampler(HallwayGameInitialInstanceSupplier initialStateSupplier,
                                   PaperTrainablePaperPolicySupplier playerPolicySupplier,
                                   EnvironmentPolicySupplier opponentPolicySupplier) {
        this.initialStateSupplier = initialStateSupplier;
        this.playerPolicySupplier = playerPolicySupplier;
        this.opponentPolicySupplier = opponentPolicySupplier;
    }

    public List<PaperEpisode> sampleEpisodes(int episodeBatchSize) {
        List<PaperEpisode> paperEpisodeHistoryList = new ArrayList<>();
        logger.info("Sampling [{}] episodes started", episodeBatchSize);
        for (int j = 0; j < episodeBatchSize; j++) {
            logger.info("Running [{}]th paperEpisode", j);
            ImmutableStateImpl initialGameState = initialStateSupplier.createInitialState();
            PaperPolicy paperPolicy = playerPolicySupplier.initializePolicyWithExploration(initialGameState);
            EnvironmentPolicy opponentPolicy = opponentPolicySupplier.initializePolicy(initialGameState);
            PaperEpisode paperEpisode = new PaperEpisode(initialGameState, paperPolicy, opponentPolicy);
            paperEpisode.runEpisode();
            paperEpisodeHistoryList.add(paperEpisode);
        }
        return paperEpisodeHistoryList;
    }

}
