package vahy.impl.learning;

import vahy.api.episode.Episode;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.learning.model.TrainablePolicySupplier;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.reward.DoubleVectorialReward;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicySupplier;
import vahy.impl.episode.EpisodeImpl;
import vahy.impl.model.observation.DoubleVector;

import java.util.ArrayList;
import java.util.List;

public class RolloutGameSampler<
    TAction extends Action,
    TReward extends DoubleVectorialReward,
    TObservation extends DoubleVector,
    TState extends State<TAction, TReward, TObservation, TState>> {

    private final InitialStateSupplier<TAction, TReward, TObservation, TState> initialStateSupplier;
    private final TrainablePolicySupplier<TAction, TReward, TObservation, TState> playerPolicySupplier;
    private final PolicySupplier<TAction, TReward, TObservation, TState> opponentPolicySupplier;

    public RolloutGameSampler(InitialStateSupplier<TAction, TReward, TObservation, TState> initialStateSupplier,
                              TrainablePolicySupplier<TAction, TReward, TObservation, TState> playerPolicySupplier,
                              PolicySupplier<TAction, TReward, TObservation, TState> opponentPolicySupplier) {
        this.initialStateSupplier = initialStateSupplier;
        this.playerPolicySupplier = playerPolicySupplier;
        this.opponentPolicySupplier = opponentPolicySupplier;
    }

    public List<Episode<TAction, TReward, TObservation, TState>> sampleEpisodes(int episodeBatchSize) {
        List<Episode<TAction, TReward, TObservation, TState>> episodeHistoryList = new ArrayList<>();
        for (int j = 0; j < episodeBatchSize; j++) {
            episodeHistoryList.clear(); // TODO: this is shit
            TState initialGameState = initialStateSupplier.createInitialState();
            Policy<TAction, TReward, TObservation, TState> policy = playerPolicySupplier.initializePolicyWithExploration(initialGameState);
            Policy<TAction, TReward, TObservation, TState> opponentPolicy = opponentPolicySupplier.initializePolicy(initialGameState);
            Episode<TAction, TReward, TObservation, TState> episode = new EpisodeImpl<>(initialGameState, policy, opponentPolicy);
            episode.runEpisode();
            episodeHistoryList.add(episode);
        }
        return episodeHistoryList;
    }
}
