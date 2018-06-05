package vahy.impl.learning;

import vahy.api.episode.Episode;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.Reward;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicySupplier;
import vahy.impl.episode.EpisodeImpl;

import java.util.ArrayList;
import java.util.List;

public class MonteCarloGameSampler<TAction extends Action, TReward extends Reward, TObservation extends Observation> {

    private final InitialStateSupplier<TAction, TReward, TObservation> initialStateSupplier;
    private final PolicySupplier<TAction, TReward, TObservation> playerPolicySupplier;
    private final PolicySupplier<TAction, TReward, TObservation> opponentPolicySupplier;

    public MonteCarloGameSampler(InitialStateSupplier<TAction, TReward, TObservation> initialStateSupplier,
                                 PolicySupplier<TAction, TReward, TObservation> playerPolicySupplier,
                                 PolicySupplier<TAction, TReward, TObservation> opponentPolicySupplier) {
        this.initialStateSupplier = initialStateSupplier;
        this.playerPolicySupplier = playerPolicySupplier;
        this.opponentPolicySupplier = opponentPolicySupplier;
    }

    public List<List<StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>>>> sampleEpisodes(int episodeBatchSize) {
        List<List<StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>>>> episodeHistoryList = new ArrayList<>();
        for (int j = 0; j < episodeBatchSize; j++) {
            episodeHistoryList.clear();
            State<TAction, TReward, TObservation> initialGameState = initialStateSupplier.createInitialState();
            Policy<TAction, TReward, TObservation> policy = playerPolicySupplier.initializePolicy(initialGameState);
            Policy<TAction, TReward, TObservation> opponentPolicy = opponentPolicySupplier.initializePolicy(initialGameState);
            Episode<TAction, TReward, TObservation> episode = new EpisodeImpl<>(initialGameState, policy, opponentPolicy);
            episodeHistoryList.add(episode.runEpisode());
        }
        return episodeHistoryList;
    }
}
