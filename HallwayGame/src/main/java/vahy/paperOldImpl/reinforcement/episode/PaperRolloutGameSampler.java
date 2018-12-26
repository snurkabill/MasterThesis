package vahy.paperOldImpl.reinforcement.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.environment.state.HallwayStateImpl;
import vahy.paperOldImpl.policy.EnvironmentPolicySupplier;
import vahy.paperOldImpl.policy.PaperPolicy;
import vahy.paperOldImpl.policy.PaperTrainablePaperPolicySupplier;
import vahy.environment.agent.policy.environment.EnvironmentPolicy;
import vahy.game.HallwayGameInitialInstanceSupplier;

import java.util.ArrayList;
import java.util.List;

public class PaperRolloutGameSampler {

    private static final Logger logger = LoggerFactory.getLogger(PaperRolloutGameSampler.class.getName());

    private final HallwayGameInitialInstanceSupplier initialStateSupplier;
    private final PaperTrainablePaperPolicySupplier playerPolicySupplier;
    private final EnvironmentPolicySupplier opponentPolicySupplier;
    private final int stepCountLimit;

    public PaperRolloutGameSampler(HallwayGameInitialInstanceSupplier initialStateSupplier,
                                   PaperTrainablePaperPolicySupplier playerPolicySupplier,
                                   EnvironmentPolicySupplier opponentPolicySupplier, int stepCountLimit) {
        this.initialStateSupplier = initialStateSupplier;
        this.playerPolicySupplier = playerPolicySupplier;
        this.opponentPolicySupplier = opponentPolicySupplier;
        this.stepCountLimit = stepCountLimit;
    }

    public List<PaperEpisode> sampleEpisodes(int episodeBatchSize) {
        List<PaperEpisode> paperEpisodeHistoryList = new ArrayList<>();
        logger.info("Sampling [{}] episodes started", episodeBatchSize);
        for (int j = 0; j < episodeBatchSize; j++) {
            logger.info("Running [{}]th paperEpisode", j);
            HallwayStateImpl initialGameState = initialStateSupplier.createInitialState();
            PaperPolicy paperPolicy = playerPolicySupplier.initializePolicyWithExploration(initialGameState);
            EnvironmentPolicy opponentPolicy = opponentPolicySupplier.initializePolicy(initialGameState);
            PaperEpisode paperEpisode = new PaperEpisode(initialGameState, paperPolicy, opponentPolicy, stepCountLimit);
            paperEpisode.runEpisode();
            paperEpisodeHistoryList.add(paperEpisode);
        }
        return paperEpisodeHistoryList;
    }

}
