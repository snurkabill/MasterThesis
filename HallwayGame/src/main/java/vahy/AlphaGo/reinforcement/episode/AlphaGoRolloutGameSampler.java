package vahy.AlphaGo.reinforcement.episode;

import vahy.AlphaGo.policy.AlphaGoEnvironmentPolicySupplier;
import vahy.AlphaGo.policy.AlphaGoPolicy;
import vahy.AlphaGo.policy.AlphaGoTrainablePolicySupplier;
import vahy.environment.agent.policy.environment.EnvironmentPolicy;
import vahy.environment.state.ImmutableStateImpl;
import vahy.game.HallwayGameInitialInstanceSupplier;

import java.util.ArrayList;
import java.util.List;

public class AlphaGoRolloutGameSampler {

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
        for (int j = 0; j < episodeBatchSize; j++) {
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
