package vahy.AlphaGo.reinforcement.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.AlphaGo.policy.AlphaGoEnvironmentPolicySupplier;
import vahy.AlphaGo.policy.AlphaGoPolicy;
import vahy.AlphaGo.policy.AlphaGoTrainablePolicySupplier;
import vahy.environment.agent.policy.environment.EnvironmentPolicy;
import vahy.environment.state.ImmutableStateImpl;
import vahy.game.HallwayGameInitialInstanceSupplier;

import java.util.ArrayList;
import java.util.List;

public class AlphaGoRolloutGameSampler {

    private static final Logger logger = LoggerFactory.getLogger(AlphaGoRolloutGameSampler.class.getName());

    private final HallwayGameInitialInstanceSupplier initialStateSupplier;
    private final AlphaGoTrainablePolicySupplier playerPolicySupplier;
    private final AlphaGoEnvironmentPolicySupplier opponentPolicySupplier;

    public AlphaGoRolloutGameSampler(HallwayGameInitialInstanceSupplier initialStateSupplier,
                                     AlphaGoTrainablePolicySupplier playerPolicySupplier,
                                     AlphaGoEnvironmentPolicySupplier opponentPolicySupplier) {
        this.initialStateSupplier = initialStateSupplier;
        this.playerPolicySupplier = playerPolicySupplier;
        this.opponentPolicySupplier = opponentPolicySupplier;
    }

    public List<AlphaGoEpisode> sampleEpisodes(int episodeBatchSize) {
        List<AlphaGoEpisode> episodeHistoryList = new ArrayList<>();
        logger.info("Sampling [{}] episodes started", episodeBatchSize);
        for (int j = 0; j < episodeBatchSize; j++) {
            logger.info("Running [{}]th episode", j);
            ImmutableStateImpl initialGameState = initialStateSupplier.createInitialState();
            AlphaGoPolicy policy = playerPolicySupplier.initializePolicyWithExploration(initialGameState);
            EnvironmentPolicy opponentPolicy = opponentPolicySupplier.initializePolicy(initialGameState);
            AlphaGoEpisode episode = new AlphaGoEpisode(initialGameState, policy, opponentPolicy);
            episode.runEpisode();
            episodeHistoryList.add(episode);
        }
        return episodeHistoryList;
    }

}
